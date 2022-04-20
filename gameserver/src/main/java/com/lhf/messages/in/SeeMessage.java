package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class SeeMessage extends Command {

    private String thing;

    SeeMessage(String payload) {
        super(CommandMessage.SEE, payload, true);
        thing = payload;
    }

    public String getThing() {
        return thing;
    }

}
