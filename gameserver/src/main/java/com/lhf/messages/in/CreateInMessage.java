package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class CreateInMessage extends Command {
    CreateInMessage(String payload) {
        super(CommandMessage.CREATE, payload, true);
        this.addPreposition("with");
        this.addPreposition("as");
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() == 1
                && this.indirects.size() >= 1 && this.indirects.containsKey("with");
    }

    public String getUsername() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public String getPassword() {
        return this.indirects.getOrDefault("with", null);
    }

    public String vocationRequest() {
        return this.indirects.getOrDefault("as", null);
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
