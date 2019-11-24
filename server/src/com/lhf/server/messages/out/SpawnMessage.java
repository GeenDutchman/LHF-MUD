package com.lhf.server.messages.out;

public class SpawnMessage extends OutMessage {

    private String username;

    public SpawnMessage(String newUserName) {
        this.username = newUserName;
    }

    @Override
    public String toString() {
        return username + " has spawned in this room.";
    }
}
