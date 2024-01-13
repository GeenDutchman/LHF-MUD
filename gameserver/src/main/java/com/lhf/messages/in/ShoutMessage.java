package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class ShoutMessage extends CommandAdapter {

    public ShoutMessage(Command command) {
        super(command);
    }

    public String getMessage() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
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
