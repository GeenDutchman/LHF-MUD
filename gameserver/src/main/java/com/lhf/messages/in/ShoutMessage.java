package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class ShoutMessage extends Command {

    ShoutMessage(String payload) {
        super(CommandMessage.SHOUT, payload, true);
    }

    public String getMessage() {
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
        String message = this.getMessage();
        if (message != null) {
            sj.add(message);
        } else {
            sj.add("No message!");
        }
        return sj.toString();
    }

}
