package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class SayMessage extends CommandAdapter {
    SayMessage(Command command) {
        super(command);
    }

    public String getMessage() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    public String getTarget() {
        return this.getIndirects().getOrDefault(Prepositions.TO, null);

    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Message:");
        String message = this.getMessage();
        if (message != null) {
            sj.add(message);
        } else {
            sj.add("No message!");
        }
        sj.add("Target:");
        String target = this.getTarget();
        if (target != null) {
            sj.add(target);
        } else {
            sj.add("No recipient");
        }
        return sj.toString();
    }

}
