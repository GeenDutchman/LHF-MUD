package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;

public class FightOverHandler extends AIHandler {

    public FightOverHandler() {
        super(GameEventType.FIGHT_OVER);
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (event.getEventType().equals(GameEventType.FIGHT_OVER) && bai.getNpc().isInBattle()) {
            bai.getNpc().getHarmMemories().reset();
        }
    }

}
