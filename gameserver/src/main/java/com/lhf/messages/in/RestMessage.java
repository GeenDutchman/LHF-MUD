package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class RestMessage extends Command {
    RestMessage(String payload) {
        super(CommandMessage.REST, payload, true);
    }
}
