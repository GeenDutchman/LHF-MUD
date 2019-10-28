package com.lhf.messages.in;

public class TakeMessage extends InMessage {
    private String target;
    public TakeMessage(String arguments) {
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }
}
