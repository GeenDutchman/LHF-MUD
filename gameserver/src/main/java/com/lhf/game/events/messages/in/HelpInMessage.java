package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class HelpInMessage extends Command {

    protected HelpInMessage(String payload) {
        super(CommandMessage.HELP, payload, true);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getGameEventType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Payload:").add(this.getWhole());
        return sj.toString();
    }
}
