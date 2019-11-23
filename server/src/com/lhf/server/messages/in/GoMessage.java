package com.lhf.server.messages.in;

public class GoMessage extends InMessage{

    private String direction;

    GoMessage(String payload) {
        direction = payload;
    }

    public String getDirection() {
        return direction;
    }
}
