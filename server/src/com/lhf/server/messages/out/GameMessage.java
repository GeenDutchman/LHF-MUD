package com.lhf.server.messages.out;

public class GameMessage extends OutMessage{
    private String message;

    public GameMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
