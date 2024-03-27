package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.BattleCreatureFledEvent;
import com.lhf.messages.events.GameEvent;

public class FleeHandler extends AIHandler {

    public FleeHandler() {
        super(GameEventType.FLEE);
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (event.getEventType().equals(GameEventType.FLEE)) {
            BattleCreatureFledEvent flee = (BattleCreatureFledEvent) event;
            if (flee.isFled() && flee.getRunner() != null) {
                if (flee.getRunner() == bai.getNpc()) {
                    bai.getNpc().getHarmMemories().reset();
                }
            }
        }
    }

}
