package com.lhf.messages;

public class SayMessage extends UserMessage {
    String message;
    public SayMessage(String payload) {
        message = payload;
    }

    @Override
    public String toString() {
        return message;
    }
}
