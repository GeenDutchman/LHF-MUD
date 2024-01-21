package com.lhf.game.creature.intelligence.handlers;

import com.lhf.Taggable;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomExitedEvent;

public class RoomExitHandler extends AIHandler {
    public RoomExitHandler() {
        super(GameEventType.ROOM_EXITED);
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (GameEventType.ROOM_EXITED.equals(event.getEventType())) {
            RoomExitedEvent slr = (RoomExitedEvent) event;
            if (slr.getLeaveTaker() != null) {
                INonPlayerCharacter npc = bai.getNpc();
                if (npc.getConvoTree() != null) {
                    npc.getConvoTree().forgetBookmark(slr.getLeaveTaker());
                }
                if (npc.getLeaderName() != null && npc.getLeaderName().equals(Taggable.extract(slr.getLeaveTaker()))) {
                    if (slr.getWhichWay() != null) {
                        bai.ProcessString("GO " + slr.getWhichWay().toString());
                    } else if (slr.getBecauseOf() != null) {
                        bai.ProcessString("INTERACT " + Taggable.extract(slr.getBecauseOf()));
                    }
                }
            }
        }
    }

}
