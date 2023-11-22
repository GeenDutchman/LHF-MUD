package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class ExitMessage extends Command {
    ExitMessage(String payload) {
        super(CommandMessage.EXIT, payload, true);
    }

    public String toString() {
        return "Good Bye";
    }

}
