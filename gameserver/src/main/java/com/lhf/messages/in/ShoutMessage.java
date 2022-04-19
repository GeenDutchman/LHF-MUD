package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class ShoutMessage extends Command {
    private String message;

    ShoutMessage(String payload) {
        super(CommandMessage.SHOUT, payload, true);
        message = payload;
    }

    public String getMessage() {
        return message;
    }

}
