package com.lhf.game.creature.intelligence.handlers;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.AggroHighwaterChooser;
import com.lhf.game.creature.intelligence.actionChoosers.BattleStatsChooser;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.game.creature.intelligence.actionChoosers.VocationChooser;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.events.messages.CommandContext;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.events.messages.CommandContext.Reply;
import com.lhf.game.events.messages.out.BattleTurnMessage;
import com.lhf.game.events.messages.out.OutMessage;
import com.lhf.game.events.messages.out.SpellEntryMessage;
import com.lhf.game.events.messages.out.StatsOutMessage;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.game.magic.SpellEntry;
import com.lhf.game.map.Directions;

public class BattleTurnHandler extends AIHandler {

    private final TreeSet<AIChooser<String>> targetChoosers;

    public record TargetLists(List<Map.Entry<String, Double>> enemies, List<Map.Entry<String, Double>> allies) {
    }

    private final DiceD100 roller;

    public BattleTurnHandler() {
        super(OutMessageType.BATTLE_TURN);
        this.roller = new DiceD100(1);
        this.targetChoosers = new TreeSet<>();

        this.targetChoosers.add(new RandomTargetChooser(this.roller));
        this.targetChoosers.add(new BattleStatsChooser());
        this.targetChoosers.add(new AggroHighwaterChooser());
        this.targetChoosers.add(new VocationChooser());
    }

    private double pick() {
        return roller.rollDice().getRoll() / (double) roller.getType().getType();
    }

    public TargetLists chooseTargets(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction) {
        if (battleMemories == null || battleMemories.isEmpty()) {
            return new TargetLists(new ArrayList<>(), new ArrayList<>());
        }
        Map<Boolean, Set<BattleStatRecord>> partitioned = battleMemories.get().getRecords().stream()
                .filter(stat -> stat != null).collect(
                        Collectors.partitioningBy(
                                stat -> stat.getTargetName().equals(harmMemories.getOwnerName())
                                        || (myFaction != null && myFaction.allied(stat.getFaction())),
                                Collectors.toSet()));
        SortedMap<String, Double> possEnemies = this.targetChoosers.stream()
                .flatMap(chooser -> chooser
                        .choose(partitioned.getOrDefault(false, null), harmMemories, List.of())
                        .entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                        Collectors.summingDouble(Map.Entry::getValue)));

        SortedMap<String, Double> possAllies = this.targetChoosers.stream()
                .flatMap(chooser -> chooser
                        .choose(partitioned.getOrDefault(true, null), harmMemories, List.of())
                        .entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                        Collectors.summingDouble(Map.Entry::getValue)));

        if (harmMemories != null) {
            possEnemies.remove(harmMemories.getOwnerName());
            possAllies.merge(harmMemories.getOwnerName(), AIChooser.MIN_VALUE, (a, b) -> a + b);
        }
        List<Map.Entry<String, Double>> enemyList = possEnemies.entrySet().stream()
                .sorted((e1, e2) -> -1 * e1.getValue().compareTo(e2.getValue())).toList();
        List<Map.Entry<String, Double>> allyList = possAllies.entrySet().stream()
                .sorted((e1, e2) -> -1 * e1.getValue().compareTo(e2.getValue())).toList();

        TargetLists lists = new TargetLists(enemyList, allyList);

        this.logger.fine(() -> String.format("Target list: %s", lists));
        return lists;
    }

    public void meleeAttackTargets(BasicAI bai, List<Map.Entry<String, Double>> targetList) {
        for (Map.Entry<String, Double> targetEntry : targetList) {
            CommandContext.Reply reply = bai.ProcessString("attack " + targetEntry.getKey());
            this.logger
                    .info(() -> String.format("Attacking target %s has reply: %s", targetEntry, reply.toString()));
            if (reply.getMessages().stream()
                    .noneMatch(message -> message.getOutType().equals(OutMessageType.BAD_TARGET_SELECTED))) {
                return;
            }
        }

        bai.log(Level.WARNING, () -> String.format("Unable to attack anyone, passing: %s", bai.ProcessString("PASS")));
    }

    // Returns empty if not to flee, otherwise populated with "flee <direction>"
    private Optional<String> processFlee(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction) {
        if (battleMemories.isEmpty()) {
            return Optional.empty();
        }
        Optional<HealthBuckets> selfHealthBucket = battleMemories.get().getRecords().stream()
                .filter(stat -> stat != null && harmMemories.getOwnerName().equals(stat.getTargetName()))
                .map(stat -> stat.getBucket())
                .findFirst();

        boolean shouldFlee = selfHealthBucket.isPresent()
                && selfHealthBucket.get().compareTo(HealthBuckets.CRITICALLY_INJURED) <= 0;
        if (shouldFlee) {
            final double fleeDecision = this.pick();

            final double stayForFriendsChance = 0.2;
            final double beserkerChance = 0.2;
            final double retributionChance = 0.2;

            // Introduce a chance for various behaviors
            if (fleeDecision < stayForFriendsChance) {
                // 20% chance of protecting allies instead of fleeing
                DoubleSummaryStatistics allyStats = battleMemories.get().getRecords().stream()
                        .filter(stat -> stat != null && (myFaction.allied(stat.getFaction())
                                || stat.getTargetName().equals(harmMemories.getOwnerName())))
                        .collect(Collectors.summarizingDouble(stat -> stat.getBucket().getValue()));
                DoubleSummaryStatistics enemyStats = battleMemories.get().getRecords().stream()
                        .filter(stat -> stat != null && (myFaction.competing(stat.getFaction())
                                && !stat.getTargetName().equals(harmMemories.getOwnerName())))
                        .collect(Collectors.summarizingDouble(stat -> stat.getBucket().getValue()));
                if (allyStats.getAverage() > enemyStats.getAverage()) {
                    shouldFlee = false;
                }
                if (allyStats.getCount() > 1) {
                    shouldFlee = false;
                }
            } else if (fleeDecision < stayForFriendsChance + beserkerChance) {
                shouldFlee = false;
            } else if (fleeDecision < stayForFriendsChance + beserkerChance + retributionChance) {
                if (harmMemories.getLastAttackerName().isPresent() && harmMemories.getLastMassAttackerName().isPresent()
                        && harmMemories.getLastAttackerName().get()
                                .equals(harmMemories.getLastMassAttackerName().get())) {
                    shouldFlee = false;
                }
            }
            // If none of the special conditions are met, flee.
        }

        if (!shouldFlee) {
            return Optional.empty();
        }
        final double directionDecision = this.pick();
        if (directionDecision < 0.25) {
            return Optional.of("FLEE " + Directions.NORTH.toString());
        } else if (directionDecision < 0.50) {
            return Optional.of("FLEE " + Directions.SOUTH.toString());
        } else if (directionDecision < 0.75) {
            return Optional.of("FLEE " + Directions.EAST.toString());
        } else {
            return Optional.of("FLEE " + Directions.WEST.toString());
        }

    }

    private Optional<String> getSpellChoice(BasicAI bai, TargetLists targetList) {
        Optional<String> command = Optional.empty();
        Vocation vocation = bai.getNpc().getVocation();
        if (vocation == null) {
            return command;
        }
        final double offensiveFocus = VocationName.HEALER.equals(bai.getNpc().getVocation().getVocationName()) ? 0.2
                : 0.8;
        if (bai.getNpc().getVocation().getVocationName().isCubeHolder()) {
            Optional<SpellEntryMessage> spellbookEntries = bai.ProcessString("SPELLBOOK").getMessages()
                    .stream()
                    .filter(outMessage -> outMessage != null
                            && OutMessageType.SPELL_ENTRY.equals((outMessage.getOutType())))
                    .map(outMessage -> ((SpellEntryMessage) outMessage)).findFirst();
            if (spellbookEntries.isPresent()) {
                try {
                    SpellEntry spellEntry = spellbookEntries.get().getEntries().stream()
                            .filter(entry -> entry != null)
                            .collect(Collectors.toMap(spellentry -> spellentry,
                                    spellentry -> (spellentry.aiScore() / (spellentry.getLevel().toInt() + 0.1))
                                            * (spellentry.isOffensive() ? offensiveFocus : 1 - offensiveFocus),
                                    (scorea, scoreb) -> (scorea + scoreb) / 2, TreeMap::new))
                            .firstKey();
                    if (spellEntry instanceof CreatureTargetingSpellEntry) {
                        final CreatureTargetingSpellEntry targetedSpell = (CreatureTargetingSpellEntry) spellEntry;
                        final List<Map.Entry<String, Double>> listToPeruse = targetedSpell.isOffensive()
                                ? targetList.enemies()
                                : targetList.allies();
                        if (targetedSpell.isSingleTarget()) {
                            command = Optional.of("Cast " + targetedSpell.getInvocation() + " at "
                                    + listToPeruse.get(0).getKey());
                        } else {
                            command = Optional.of(listToPeruse.stream()
                                    .map(targetEntry -> targetEntry.getKey()).collect(Collectors.joining(" at ",
                                            "Cast " + targetedSpell.getInvocation() + " at ", "")));
                        }
                    } else {
                        command = Optional.of("Cast " + spellEntry.getInvocation());
                    }
                } catch (NoSuchElementException e) {
                    command = Optional.empty();
                }

            }
        }
        return command;
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        bai.ProcessString("SEE");
        if (!this.outMessageType.equals(msg.getOutType()) || !bai.getNpc().isInBattle()) {
            return;
        }
        BattleTurnMessage btm = (BattleTurnMessage) msg;
        Reply reply = bai.ProcessString("STATS");
        Optional<StatsOutMessage> statsOutOpt = reply.getMessages().stream()
                .filter(outMessage -> outMessage != null && OutMessageType.STATS.equals(outMessage.getOutType()))
                .map(outMessage -> ((StatsOutMessage) outMessage)).findFirst();

        if (statsOutOpt.isEmpty()) {
            this.logger.warning(() -> String
                    .format("%s cannot get battle stats, and thus cannot battle: attempting PASS", bai.toString()));
            reply = bai.ProcessString("PASS");
            if (!reply.isHandled()) {
                String logMessage = String.format("PASS not handled: %s", reply.toString());
                this.logger.warning(logMessage);
            }
            return;
        }

        HarmMemories harmMemories = bai.getNpc().getHarmMemories();
        CreatureFaction myFaction = bai.getNpc().getFaction();
        if (btm.isYesTurn() && bai.getNpc().equals(btm.getMyTurn())) {

            Optional<String> command = processFlee(statsOutOpt,
                    harmMemories,
                    myFaction);
            if (command.isPresent()) {
                // CommandContext.Reply reply = bai.ProcessString(command.get());
                bai.ProcessString(command.get());
                return;
            }

            TargetLists targetList = this.chooseTargets(statsOutOpt,
                    harmMemories,
                    myFaction);

            command = getSpellChoice(bai, targetList);
            if (command.isPresent()) {
                // CommandContext.Reply reply = bai.ProcessString(command.get());
                bai.ProcessString(command.get());
                return;
            }

            if (targetList.enemies.size() > 0) {
                this.meleeAttackTargets(bai, targetList.enemies());
            } else {
                bai.ProcessString("Pass");
            }

        }
    }
}
