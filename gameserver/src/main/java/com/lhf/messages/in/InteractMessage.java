package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class InteractMessage extends Command {

    InteractMessage(String payload) {
        super(CommandMessage.INTERACT, payload, true);
    }

    public String getObject() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() >= 1 && this.indirects.size() == 0;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString()).add("Interactable:").add(this.getObject());
        return sj.toString();
    }

}
