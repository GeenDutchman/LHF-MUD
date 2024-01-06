package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class CreateInMessage extends CommandAdapter {
    CreateInMessage(Command command) {
        super(command);
    }

    public String getUsername() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0).trim();
    }

    public String getPassword() {
        return this.getIndirects().getOrDefault(Prepositions.WITH, null);
    }

    public String vocationRequest() {
        return this.getIndirects().getOrDefault(Prepositions.AS, null);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Username:");
        if (this.getUsername() != null) {
            sj.add(this.getUsername());
        } else {
            sj.add("not provided");
        }
        sj.add("Password:");
        if (this.getPassword() != null) {
            sj.add("provided");
        } else {
            sj.add("not provided");
        }
        if (this.vocationRequest() != null) {
            sj.add("Requested to be: " + this.vocationRequest());
        }
        return sj.toString();
    }

}
