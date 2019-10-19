package com.lhf.messages.out;

public class GameMessage extends OutMessage{
    String message;

    public GameMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
