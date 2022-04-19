package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class DropMessage extends Command {
    private String target;

    DropMessage(String arguments) {
        super(CommandMessage.DROP, arguments, true);
    }

    public String getTarget() {
        return target;
    }

}
