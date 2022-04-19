package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class UseMessage extends Command {
    private String usefulItem = "";
    private String targetName = "";

    UseMessage(String payload) {
        super(CommandMessage.USE, payload, true);
        this.addPreposition("on");
    }

    public String getUsefulItem() {
        return usefulItem;
    }

    public String getTarget() {
        return targetName;
    }

    @Override
    public String toString() {
        return "Using " + this.usefulItem + " on " + this.targetName;
    }

}
