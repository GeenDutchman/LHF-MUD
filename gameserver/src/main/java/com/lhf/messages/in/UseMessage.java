package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class UseMessage extends InMessage {
    private String usefulItem = "";
    private String targetName = "";

    static final private String[] prepositionFlags = { "on" };

    UseMessage(String payload) {
        String[] words = payload.split(" ");
        boolean usedFlags = areFlags(words, prepositionFlags);
        if (usedFlags) { // use item on target
            words = prepositionSeparator(words, prepositionFlags, 2);
            usefulItem += words[0];
            targetName += words[1];
        } else { // use item
            usefulItem += payload;
        }

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

    @Override
    public CommandMessage getType() {
        return CommandMessage.USE;
    }
}
