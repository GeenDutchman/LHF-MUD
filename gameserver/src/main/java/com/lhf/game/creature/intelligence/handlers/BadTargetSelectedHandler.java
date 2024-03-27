package com.lhf.game.creature.intelligence.handlers;

import java.util.logging.Level;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.GameEvent;

public class BadTargetSelectedHandler extends AIHandler {

    public BadTargetSelectedHandler() {
        super(GameEventType.BAD_TARGET_SELECTED);
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (event.getEventType().equals(GameEventType.BAD_TARGET_SELECTED) && bai.getNpc().isInBattle()) {
            BadTargetSelectedEvent btsm = (BadTargetSelectedEvent) event;
            bai.log(Level.WARNING, () -> String.format("Selected a bad target: %s with possible targets", btsm,
                    btsm.getPossibleTargets()));
        }
    }

}
