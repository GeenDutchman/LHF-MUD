package com.lhf.messages.in;


public class UseMessage extends InMessage {
    private String usefulItem = "";
    private String targetName = "";

    static final private String[] prepositionFlags = {"on"};

    public UseMessage(String payload) {
        String[] words = payload.split(" ");
        boolean usedFlags = areFlags(words, prepositionFlags);
        if (usedFlags) { // use item on target
            words = prepositionSeparator(words, prepositionFlags, 2);
            usefulItem += words[0];
            targetName += words[1];
        } else if (words.length >= 2) { // use item target
            usefulItem += words[0];
            targetName += words[1];
        } else {
            usefulItem += words[0];
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
}
