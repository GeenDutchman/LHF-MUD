package com.lhf.game.creature.intelligence.handlers;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.text.html.Option;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.AggroHighwaterChooser;
import com.lhf.game.creature.intelligence.actionChoosers.BattleStatsChooser;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.game.creature.intelligence.actionChoosers.VocationChooser;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.HealthBuckets;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.game.magic.SpellEntry;
import com.lhf.game.map.Directions;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpellEntryMessage;
import com.lhf.messages.out.StatsOutMessage;

public class BattleTurnHandler extends AIHandler {

    private final TreeSet<AIChooser<String>> targetChoosers;

    public record TargetLists(List<Map.Entry<String, Double>> enemies, List<Map.Entry<String, Double>> allies) {
    }

    public BattleTurnHandler() {
        super(OutMessageType.BATTLE_TURN);
        this.targetChoosers = new TreeSet<>();

        this.targetChoosers.add(new RandomTargetChooser());
        this.targetChoosers.add(new BattleStatsChooser());
        this.targetChoosers.add(new AggroHighwaterChooser());
        this.targetChoosers.add(new VocationChooser());
    }

    public TargetLists chooseTargets(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction, Collection<OutMessage> outMessages) {
        SortedMap<String, Double> possEnemies = this.targetChoosers.stream()
                .flatMap(chooser -> chooser
                        .choose(battleMemories, harmMemories, CreatureFaction.competeSet(myFaction), List.of())
                        .entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                        Collectors.summingDouble(Map.Entry::getValue)));

        SortedMap<String, Double> possAllies = this.targetChoosers.stream()
                .flatMap(chooser -> chooser
                        .choose(battleMemories, harmMemories, CreatureFaction.allySet(myFaction), List.of())
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

        this.logger.warning(() -> String.format("Unable to attack anyone, passing: %s", bai.ProcessString("PASS")));
    }

    // Returns empty if not to flee, otherwise populated with "flee <direction>"
    private Optional<String> processFlee(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction) {
        Optional<HealthBuckets> selfHealthBucket = battleMemories.get().getRecords().stream()
                .filter(stat -> stat != null && harmMemories.getOwnerName().equals(stat.getTargetName()))
                .map(stat -> stat.getBucket())
                .findFirst();

        final Dice roller = new DiceD100(1);
        boolean shouldFlee = selfHealthBucket.isPresent()
                && selfHealthBucket.get().compareTo(HealthBuckets.CRITICALLY_INJURED) <= 0;
        if (shouldFlee) {
            final double fleeDecision = (double) roller.rollDice().getRoll() / roller.getType().getType();

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
        final double directionDecision = (double) roller.rollDice().getRoll() / roller.getType().getType();
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

    public Optional<String> process(Optional<StatsOutMessage> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction, Collection<OutMessage> outMessages) {

        Optional<String> command = processFlee(battleMemories, harmMemories, myFaction);
        if (command.isPresent()) {
            return command;
        }
        if (battleMemories.isPresent() && battleMemories.get().getRecords().stream()
                .anyMatch(stat -> stat != null && stat.getTargetName().equals(harmMemories.getOwnerName())
                        && stat.getVocation().getVocationName().isCubeHolder())) {
            // Check if the Monster is a Spellcaster
            if (monster.hasSpells()) {
                // Decide which spell to cast based on your logic
                Spell selectedSpell = monster.selectSpell();

                if (selectedSpell.isTargeted()) {
                    // If the spell targets specific Players, select the target(s)
                    List<Player> spellTargets = monster.selectSpellTargets(selectedSpell);
                    return new CastSpellAction(selectedSpell, spellTargets);
                } else {
                    // If the spell affects all players in the room, cast it without specific
                    // targets
                    return new CastSpellAction(selectedSpell, null);
                }
            }
        }

        // Calculate probabilities based on damage criteria
        double recentDamageProbability = 0.3;
        double mostDamageProbability = 0.2;
        double randomAttackProbability = 1.0 - (recentDamageProbability + mostDamageProbability);

        double randomValue = Math.random();
        if (randomValue < recentDamageProbability && recentDamagePlayer != null && recentDamagePlayer.health > 0) {
            return new AttackAction(recentDamagePlayer);
        } else if (randomValue < recentDamageProbability + mostDamageProbability && mostDamagePlayer != null
                && mostDamagePlayer.health > 0) {
            return new AttackAction(mostDamagePlayer);
        } else {
            // If the random value doesn't match recent or most damage, choose a random
            // target
            List<Player> potentialTargets = getPotentialTargets();
            if (!potentialTargets.isEmpty()) {
                Player randomTarget = potentialTargets.get((int) (Math.random() * potentialTargets.size()));
                return new AttackAction(randomTarget);
            } else {
                // If no valid targets, pass the turn
                return Action.PASS;
            }
        }

    }

    private Optional<String> getSpellChoice(BasicAI bai, TargetLists targetList) {
        Optional<String> command = Optional.empty();
        final double offensiveFocus = 0.8;
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
        HarmMemories harmMemories = bai.getNpc().getHarmMemories();
        CreatureFaction myFaction = bai.getNpc().getFaction();
        if (btm.isYesTurn() && bai.getNpc().equals(btm.getMyTurn())) {

            Optional<String> command = processFlee(statsOutOpt,
                    bai.getNpc().getHarmMemories(),
                    bai.getNpc().getFaction());
            if (command.isPresent()) {
                // CommandContext.Reply reply = bai.ProcessString(command.get());
                bai.ProcessString(command.get());
                return;
            }

            TargetLists targetList = this.chooseTargets(statsOutOpt,
                bai.getNpc().getHarmMemories(),
                bai.getNpc().getFaction(),
                List.of());

            command = getSpellChoice(bai, targetList);
            if (command.isPresent()) {
                // CommandContext.Reply reply = bai.ProcessString(command.get());
                bai.ProcessString(command.get());
                return;
            }

            this.meleeAttackTargets(bai, targetList.enemies());

        }
    }
