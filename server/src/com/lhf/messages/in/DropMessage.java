package com.lhf.messages.in;

public class DropMessage extends InMessage {
    private String target;

    public DropMessage(String arguments) {
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }
}
