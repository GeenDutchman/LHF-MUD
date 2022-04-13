package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class TakeMessage extends InMessage {
    private String target;

    TakeMessage(String arguments) {
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.TAKE;
    }
}
