package com.lhf.messages.out;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.OutMessageType;

public class RoomEnteredOutMessage extends OutMessage {
    private ClientMessenger newbie;

    public RoomEnteredOutMessage(ClientMessenger newbie) {
        super(OutMessageType.ROOM_ENTERED);
        this.newbie = newbie;
    }

    @Override
    public String toString() {
        return this.newbie.getColorTaggedName() + "has entered the room";
    }

    public ClientMessenger getNewbie() {
        return newbie;
    }
}
