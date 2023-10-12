package com.lhf.game.creature.intelligence.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.AIChooser;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.AggroHighwaterChooser;
import com.lhf.game.creature.intelligence.actionChoosers.BattleStatsChooser;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.game.creature.intelligence.actionChoosers.VocationChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;
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
        if (btm.isYesTurn() && bai.getNpc().equals(btm.getMyTurn())) {

            TargetLists targetList = this.chooseTargets(statsOutOpt,
                    bai.getNpc().getHarmMemories(),
                    bai.getNpc().getFaction(),
                    List.of());

            this.meleeAttackTargets(bai, targetList.enemies());

        }
    }

}
