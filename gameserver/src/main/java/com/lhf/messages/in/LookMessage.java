package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class LookMessage extends InMessage {

    @Override
    public CommandMessage getType() {
        return CommandMessage.LOOK;
    }

}
