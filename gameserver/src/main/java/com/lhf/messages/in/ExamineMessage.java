package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class ExamineMessage extends Command {

    private String thing;

    ExamineMessage(String payload) {
        super(CommandMessage.EXAMINE, payload, true);
        thing = payload;
    }

    public String getThing() {
        return thing;
    }

}
