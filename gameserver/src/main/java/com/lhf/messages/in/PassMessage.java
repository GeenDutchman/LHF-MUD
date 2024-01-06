package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class PassMessage extends CommandAdapter {

    PassMessage(Command command) {
        super(command);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        return sj.toString();
    }
}
