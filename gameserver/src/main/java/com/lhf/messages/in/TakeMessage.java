package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class TakeMessage extends Command {
    private String target;

    TakeMessage(String arguments) {
        super(CommandMessage.TAKE, arguments, true);
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }

}
