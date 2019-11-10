package com.lhf.messages.in;

public class InteractMessage extends InMessage {
    private String object;

    public InteractMessage(String payload) {
        object = payload;
    }

    public String getObject() {
        return object;
    }
}
