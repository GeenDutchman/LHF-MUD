package com.lhf.messages.in;

import com.lhf.messages.Command;

public class ExitMessage extends CommandAdapter {
    public ExitMessage(Command command) {
        super(command);
    }

    public String toString() {
        return "Good Bye";
    }

}
