package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class InteractMessage extends Command {
    private String object;

    InteractMessage(String payload) {
        super(CommandMessage.INTERACT, payload, true);
        object = payload;
    }

    public String getObject() {
        return object;
    }

}
