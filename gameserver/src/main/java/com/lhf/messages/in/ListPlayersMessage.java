package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;

public class ListPlayersMessage extends CommandAdapter {

    ListPlayersMessage(Command command) {
        super(command);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        return sj.toString();
    }

}
