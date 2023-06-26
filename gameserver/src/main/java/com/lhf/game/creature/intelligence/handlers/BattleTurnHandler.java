package com.lhf.game.creature.intelligence.handlers;

import java.util.TreeMap;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.intelligence.actionChoosers.RandomTargetChooser;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;

public class BattleTurnHandler extends AIHandler {
    private final RandomTargetChooser randomTargetChooser;

    public BattleTurnHandler() {
        super(OutMessageType.BATTLE_TURN);
        this.randomTargetChooser = new RandomTargetChooser();
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (!this.outMessageType.equals(msg.getOutType())) {
            return;
        }
        BattleTurnMessage btm = (BattleTurnMessage) msg;
        TreeMap<Float, String> possCommands = new TreeMap<>();
    }

}
