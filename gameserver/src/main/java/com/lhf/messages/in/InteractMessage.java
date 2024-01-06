package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class InteractMessage extends CommandAdapter {

    InteractMessage(Command command) {
        super(command);
    }

    public String getObject() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString()).add("Interactable:").add(this.getObject());
        return sj.toString();
    }

}
