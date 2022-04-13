package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class SayMessage extends InMessage {
    private String message;

    SayMessage(String payload) {
        message = payload;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.SAY;
    }
}
