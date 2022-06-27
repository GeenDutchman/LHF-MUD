package com.lhf.messages.out;

import java.util.Map;

import com.lhf.messages.CommandMessage;

public class SingleHelpMessage extends OutMessage {
    private CommandMessage message;
    private String helpRetrieved;

    public SingleHelpMessage(Map<CommandMessage, String> helps, CommandMessage message) {
        this.message = message;
        this.helpRetrieved = helps.get(this.message);
    }

    @Override
    public String toString() {
        if (this.helpRetrieved != null) {
            return this.message.getColorTaggedName() + ":\n" + this.helpRetrieved;
        }
        if (this.message == null) {
            return "Help not found";
        }
        return "Help for " + this.message.toString() + " not found.";
    }

    public boolean helpFound() {
        return this.helpRetrieved != null;
    }

    public CommandMessage getMessage() {
        return message;
    }

}
