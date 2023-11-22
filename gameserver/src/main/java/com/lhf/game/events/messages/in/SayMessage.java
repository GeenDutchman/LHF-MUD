package com.lhf.game.events.messages.in;

import java.util.StringJoiner;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public class SayMessage extends Command {
    SayMessage(String payload) {
        super(CommandMessage.SAY, payload, true);
        this.addPreposition("to");
    }

    public String getMessage() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public String getTarget() {
        if (this.indirects.containsKey("to")) {
            return this.indirects.getOrDefault("to", null);
        }
        return null;
    }

    @Override
    public Boolean isValid() {
        Boolean validated = true;
        if (this.indirects.size() > 0) {
            validated = this.indirects.size() == 1 && this.indirects.containsKey("to");
        }
        return super.isValid() && this.directs.size() >= 1 && validated;
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
