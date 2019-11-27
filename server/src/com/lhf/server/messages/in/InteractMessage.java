package com.lhf.server.messages.in;

public class InteractMessage extends InMessage {
    private String object;

    InteractMessage(String payload) {
        object = payload;
    }

    public String getObject() {
        return object;
    }
}
