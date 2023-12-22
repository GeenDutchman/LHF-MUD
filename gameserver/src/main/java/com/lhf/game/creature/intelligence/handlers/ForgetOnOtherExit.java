package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomExitedEvent;

public class ForgetOnOtherExit extends AIHandler {
    public ForgetOnOtherExit() {
        super(GameEventType.ROOM_EXITED);
    }

    @Override
    public void handle(BasicAI bai, GameEvent msg) {
        if (GameEventType.ROOM_EXITED.equals(msg.getEventType())) {
            RoomExitedEvent slr = (RoomExitedEvent) msg;
            if (slr.getLeaveTaker() != null) {
                if (bai.getNpc().getConvoTree() != null) {
                    bai.getNpc().getConvoTree().forgetBookmark(slr.getLeaveTaker());
                }
            }
        }
    }

}
