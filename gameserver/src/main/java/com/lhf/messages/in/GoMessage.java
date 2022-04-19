package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class GoMessage extends Command {

    private String direction;

    GoMessage(String payload) {
        super(CommandMessage.GO, payload, true);
        direction = payload;
    }

    public String getDirection() {
        return direction;
    }

}
