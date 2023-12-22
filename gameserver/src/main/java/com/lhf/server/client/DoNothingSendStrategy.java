package com.lhf.server.client;

import com.lhf.messages.events.GameEvent;

public class DoNothingSendStrategy implements SendStrategy {

    @Override
    public void send(GameEvent toSend) {
        // Does literally nothing
    }

}
