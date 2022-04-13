package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class GoMessage extends InMessage {

    private String direction;

    GoMessage(String payload) {
        direction = payload;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.GO;
    }
}
