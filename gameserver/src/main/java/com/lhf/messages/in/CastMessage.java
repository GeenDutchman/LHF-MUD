package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class CastMessage extends InMessage {
    private String invocation = "";
    private String target = "";
    private Integer level = -1;

    static final private String[] prepositionFlags = { "at", "use" };

    CastMessage(String payload) {
        String[] words = payload.split(" ");
        boolean usedFlags = areFlags(words, prepositionFlags);
        if (usedFlags) { // cast invocation at target use level
            words = prepositionSeparator(words, prepositionFlags, 3);
            this.invocation += words[0];
            this.target += words[1];
            if (words[2].length() > 0) {
                this.level = Integer.valueOf(words[2]);
            }
            return;
        }
        this.invocation = payload;
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

    @Override
    public CommandMessage getType() {
        return CommandMessage.CAST;
    }

}
