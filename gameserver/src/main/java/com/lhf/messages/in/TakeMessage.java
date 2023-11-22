package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class TakeMessage extends Command {

    TakeMessage(String arguments) {
        super(CommandMessage.TAKE, arguments, true);
    }

    public String getTarget() {
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
        sj.add(super.toString());
        sj.add("Object:");
        String thing = this.getTarget();
        if (thing != null) {
            sj.add(thing);
        } else {
            sj.add("Nothing!");
        }
        return sj.toString();
    }
}
