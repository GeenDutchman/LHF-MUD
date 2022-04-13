package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class DropMessage extends InMessage {
    private String target;

    DropMessage(String arguments) {
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.DROP;
    }
}
