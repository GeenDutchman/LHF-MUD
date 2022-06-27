package com.lhf.messages.out;

import com.lhf.messages.ClientMessenger;

public class RoomEnteredOutMessage extends OutMessage {
    private ClientMessenger newbie;

    public RoomEnteredOutMessage(ClientMessenger newbie) {
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
