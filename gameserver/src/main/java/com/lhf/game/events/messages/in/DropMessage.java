package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class DropMessage extends Command {
    DropMessage(String arguments) {
        super(CommandMessage.DROP, arguments, true);
    }

    @Override
    public Boolean isValid() {
        return super.isValid() && this.directs.size() >= 1 && this.indirects.size() == 0;
    }

    public String getTarget() {
        return this.directs.get(0);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("What:");
        if (this.getTarget() != null) {
            sj.add(this.getTarget());
        } else {
            sj.add("not dropping anything!");
        }
        return sj.toString();
    }

}
