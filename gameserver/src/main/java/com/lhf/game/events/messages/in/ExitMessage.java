package com.lhf.game.events.messages.in;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class ExitMessage extends Command {
    ExitMessage(String payload) {
        super(CommandMessage.EXIT, payload, true);
    }

    public String toString() {
        return "Good Bye";
    }

}
