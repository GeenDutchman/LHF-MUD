package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class SeeMessage extends Command {

    SeeMessage(String payload) {
        super(CommandMessage.SEE, payload, true);
    }

    public String getThing() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() >= 0 && this.indirects.size() == 0;
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
