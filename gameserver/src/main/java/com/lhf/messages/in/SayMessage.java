package com.lhf.messages.in;

public class SayMessage extends InMessage {
    private String message;

    SayMessage(String payload) {
        message = payload;
    }

    public String getMessage() {
        return message;
    }
}
