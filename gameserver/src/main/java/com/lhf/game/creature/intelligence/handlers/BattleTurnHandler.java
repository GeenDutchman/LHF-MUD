package com.lhf.game.creature.intelligence.handlers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.AggroHighwaterChooser;
import com.lhf.game.creature.intelligence.actionChoosers.AggroStatsChooser;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.messages.CommandContext;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;

public class BattleTurnHandler extends AIHandler {
    private final RandomTargetChooser randomTargetChooser;
    private final AggroStatsChooser aggroStatsChooser;
    private final AggroHighwaterChooser aggroHighwaterChooser;

    public BattleTurnHandler() {
        super(OutMessageType.BATTLE_TURN);
        this.randomTargetChooser = new RandomTargetChooser();
        this.aggroStatsChooser = new AggroStatsChooser();
        this.aggroHighwaterChooser = new AggroHighwaterChooser();
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (!this.outMessageType.equals(msg.getOutType())) {
            return;
        }
        BattleTurnMessage btm = (BattleTurnMessage) msg;
        if (btm.isYesTurn() && bai.getNpc().equals(btm.getMyTurn())) {
            SortedMap<String, Float> possTarget = Stream
                    .of(this.randomTargetChooser.chooseTarget(bai.getBattleMemories(), bai.getNpc().getFaction()),
                            this.aggroStatsChooser.chooseTarget(bai.getBattleMemories(), bai.getNpc().getFaction()),
                            this.aggroHighwaterChooser.chooseTarget(bai.getBattleMemories(), bai.getNpc().getFaction()))
                    .flatMap(map -> map.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1 + v2,
                            TreeMap::new));

            String target = possTarget.entrySet().stream()
                    .max((e1, e2) -> e1.getValue() != null ? e1.getValue().compareTo(e2.getValue())
                            : e2.getValue().compareTo(e1.getValue()))
                    .get().getKey();
            CommandContext.Reply reply = bai.ProcessString("attack " + target);
            this.logger.info(() -> String.format("Attacking target %s has reply: %s", target, reply.toString()));
        }
    }

}
