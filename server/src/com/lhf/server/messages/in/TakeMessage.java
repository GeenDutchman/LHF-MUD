package com.lhf.server.messages.in;

public class TakeMessage extends InMessage {
    private String target;

    TakeMessage(String arguments) {
        this.target = arguments;
    }

    public String getTarget() {
        return target;
    }
}
