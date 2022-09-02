package com.lhf.messages.out;

import com.lhf.game.map.Directions;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.OutMessageType;

public class SomeoneLeftRoom extends OutMessage {
    private final ClientMessenger leaveTaker;
    private final Directions whichWay;

    public SomeoneLeftRoom(ClientMessenger leaveTaker, Directions whichWay) {
        super(OutMessageType.ROOM_EXITED);
        this.leaveTaker = leaveTaker;
        this.whichWay = whichWay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leaveTaker.getColorTaggedName()).append(" left the room");
        if (this.whichWay != null) {
            sb.append(" going ").append(this.whichWay.getColorTaggedName());
        }
        sb.append(".");
        return sb.toString();
    }

    public ClientMessenger getLeaveTaker() {
        return leaveTaker;
    }

    public Directions getWhichWay() {
        return whichWay;
    }

}
