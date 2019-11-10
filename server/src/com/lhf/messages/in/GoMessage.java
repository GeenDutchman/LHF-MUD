package com.lhf.messages.in;

public class GoMessage extends InMessage{

    private String direction;

    public GoMessage(String payload) {
        direction = payload;
    }

    public String getDirection() {
        return direction;
    }
}
