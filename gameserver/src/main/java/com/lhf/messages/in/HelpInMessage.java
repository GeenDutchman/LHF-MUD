package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class HelpInMessage extends Command {

    protected HelpInMessage(String payload) {
        super(CommandMessage.HELP, payload, true);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Payload:").add(this.getWhole());
        return sj.toString();
    }
}
