package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class StatusMessage extends InMessage {

    @Override
    public CommandMessage getType() {
        return CommandMessage.STATUS;
    }
}
