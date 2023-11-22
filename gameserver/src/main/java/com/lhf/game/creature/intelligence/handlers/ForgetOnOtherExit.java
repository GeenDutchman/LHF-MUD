package com.lhf.game.creature.intelligence.handlers;

import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.events.messages.OutMessageType;
import com.lhf.game.events.messages.out.OutMessage;
import com.lhf.game.events.messages.out.SomeoneLeftRoom;

public class ForgetOnOtherExit extends AIHandler {
    public ForgetOnOtherExit() {
        super(OutMessageType.ROOM_EXITED);
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (OutMessageType.ROOM_EXITED.equals(msg.getOutType())) {
            SomeoneLeftRoom slr = (SomeoneLeftRoom) msg;
            if (slr.getLeaveTaker() != null) {
                if (bai.getNpc().getConvoTree() != null) {
                    bai.getNpc().getConvoTree().forgetBookmark(slr.getLeaveTaker());
                }
            }
        }
    }

}
