package com.lhf.server.messages.in;

public class ShoutMessage extends InMessage {
    private String message;

    public ShoutMessage(String payload) {
        message = payload;
    }

    public String getMessage() {
        return message;
    }
}
