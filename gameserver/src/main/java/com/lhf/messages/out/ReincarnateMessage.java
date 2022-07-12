package com.lhf.messages.out;

public class ReincarnateMessage extends OutMessage {
    private final String taggedName;

    public ReincarnateMessage(String taggedName) {
        this.taggedName = taggedName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*******************************X_X*********************************************").append("\r\n");
        sb.append(this.taggedName).append(", You have died. Out of mercy you have been reborn back where you began.");
        return sb.toString();
    }

    public String getTaggedName() {
        return taggedName;
    }
}
