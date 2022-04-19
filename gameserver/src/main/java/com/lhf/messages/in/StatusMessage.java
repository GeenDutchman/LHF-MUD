package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class StatusMessage extends Command {

    protected StatusMessage(String payload) {
        super(CommandMessage.STATUS, payload, true);
    }

}
