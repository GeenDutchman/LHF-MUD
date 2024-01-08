package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class SeeMessage extends CommandAdapter {

    public SeeMessage(Command command) {
        super(command);
    }

    public String getThing() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Looking at:");
        String thing = this.getThing();
        if (thing != null) {
            sj.add(thing);
        } else {
            sj.add("things in general");
        }
        return sj.toString();
    }

}
