package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class ExitMessage extends InMessage {
    public String toString() {
        return "Good Bye";
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.EXIT;
    }
}
