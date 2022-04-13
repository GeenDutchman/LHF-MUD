package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class ExamineMessage extends InMessage {

    private String thing;

    ExamineMessage(String payload) {
        thing = payload;
    }

    public String getThing() {
        return thing;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.EXAMINE;
    }
}
