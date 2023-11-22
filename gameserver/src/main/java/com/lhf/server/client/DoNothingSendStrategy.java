package com.lhf.server.client;

import com.lhf.messages.out.OutMessage;

public class DoNothingSendStrategy implements SendStrategy {

    @Override
    public void send(OutMessage toSend) {
        // Does literally nothing
    }

}
