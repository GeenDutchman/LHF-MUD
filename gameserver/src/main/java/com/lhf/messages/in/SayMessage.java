package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class SayMessage extends Command {
    private String message;
    private String target;

    SayMessage(String payload) {
        super(CommandMessage.SAY, payload, true);
        this.addPreposition("to");
        message = payload;
    }

    public String getMessage() {
        return message;
    }

    public String getTarget() {
        return target;
    }

}
