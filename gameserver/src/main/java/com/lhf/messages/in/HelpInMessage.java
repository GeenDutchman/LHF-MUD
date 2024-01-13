package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class HelpInMessage extends CommandAdapter {

    public HelpInMessage(Command command) {
        super(command);
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
