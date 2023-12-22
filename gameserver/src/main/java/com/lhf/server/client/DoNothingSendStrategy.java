package com.lhf.server.client;

import com.lhf.messages.out.GameEvent;

public class DoNothingSendStrategy implements SendStrategy {

    @Override
    public void send(GameEvent toSend) {
        // Does literally nothing
    }

}
