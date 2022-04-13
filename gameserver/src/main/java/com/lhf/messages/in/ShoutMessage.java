package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class ShoutMessage extends InMessage {
    private String message;

    public ShoutMessage(String payload) {
        message = payload;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.SHOUT;
    }
}
