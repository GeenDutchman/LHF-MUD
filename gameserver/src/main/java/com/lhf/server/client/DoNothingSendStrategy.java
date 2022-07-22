package com.lhf.server.client;

public class DoNothingSendStrategy implements SendStrategy {

    @Override
    public void send(String toSend) {
        // Does literally nothing
    }

}
