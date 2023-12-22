package com.lhf.server.client;

import com.lhf.messages.out.GameEvent;

public interface SendStrategy {
    public void send(GameEvent toSend);
}
