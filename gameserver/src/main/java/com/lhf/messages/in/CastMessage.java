package com.lhf.messages.in;

import java.util.HashMap;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class CastMessage extends Command {
    private String invocation = "";
    private String target = "";
    private Integer level = -1;

    CastMessage(String payload) {
        super(CommandMessage.CAST, payload, true);
        this.addPreposition("at");
        this.addPreposition("use");
    }

    public String getInvocation() {
        return invocation;
    }

    public String getTarget() {
        return target;
    }

    public Integer getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "Casting a spell with invocation " + this.invocation + " at " + this.target
                + (this.level >= 0 ? " using level " + this.level.toString() : "");
    }

}
