package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class SpawnMessage extends OutMessage {

    private String username;

    public SpawnMessage(String newUserName) {
        super(OutMessageType.SPAWN);
        this.username = newUserName;
    }

    @Override
    public String toString() {
        return "<description>" + username + " has spawned in this room." + "</description>";
    }
}
