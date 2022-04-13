package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class InteractMessage extends InMessage {
    private String object;

    InteractMessage(String payload) {
        object = payload;
    }

    public String getObject() {
        return object;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.INTERACT;
    }
}
