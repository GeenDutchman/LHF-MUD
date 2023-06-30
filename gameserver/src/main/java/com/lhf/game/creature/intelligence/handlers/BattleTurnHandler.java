package com.lhf.game.creature.intelligence.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.lhf.game.battle.BattleStats.BattleStatRecord;
import com.lhf.game.creature.NonPlayerCharacter.HarmMemories;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.ActionChooser;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.AggroHighwaterChooser;
import com.lhf.game.creature.intelligence.actionChoosers.AggroStatsChooser;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.StatsOutMessage;

public class BattleTurnHandler extends AIHandler {

    private final TreeSet<ActionChooser> enemyTargetChoosers;

    public BattleTurnHandler() {
        super(OutMessageType.BATTLE_TURN);
        this.enemyTargetChoosers = new TreeSet<>();

        this.enemyTargetChoosers.add(new RandomTargetChooser());
        this.enemyTargetChoosers.add(new AggroStatsChooser());
        this.enemyTargetChoosers.add(new AggroHighwaterChooser());
    }

    public List<Map.Entry<String, Double>> chooseEnemyTarget(Optional<Collection<BattleStatRecord>> battleMemories,
            HarmMemories harmMemories,
            CreatureFaction myFaction) {
        SortedMap<String, Double> possTarget = this.enemyTargetChoosers.stream()
                .flatMap(chooser -> chooser
                        .chooseTarget(battleMemories, harmMemories, CreatureFaction.competeSet(myFaction)).entrySet()
                        .stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, TreeMap::new,
                        Collectors.summingDouble(Map.Entry::getValue)));

        if (harmMemories != null) {
            possTarget.remove(harmMemories.getOwnerName());
        }
        List<Map.Entry<String, Double>> targetList = possTarget.entrySet().stream()
                .sorted((e1, e2) -> -1 * e1.getValue().compareTo(e2.getValue())).toList();

        this.logger.fine(() -> String.format("Target list: %s", targetList));
        return targetList;
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
        Optional<Collection<BattleStatRecord>> statsOutOpt = reply.getMessages().stream()
                .filter(outMessage -> outMessage != null && OutMessageType.STATS.equals(outMessage.getOutType()))
                .map(outMessage -> ((StatsOutMessage) outMessage).getRecords()).findFirst();
        if (btm.isYesTurn() && bai.getNpc().equals(btm.getMyTurn())) {

            List<Map.Entry<String, Double>> targetList = this.chooseEnemyTarget(statsOutOpt,
                    bai.getNpc().getHarmMemories(),
                    bai.getNpc().getFaction());

            this.meleeAttackTargets(bai, targetList);

        }
    }

}
