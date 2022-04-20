package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class HelpInMessage extends Command {

    protected HelpInMessage(String payload) {
        super(CommandMessage.HELP, payload, true);
    }

}
