package com.lhf.server.client;

import com.lhf.messages.events.GameEvent;

public interface SendStrategy {
    public void send(GameEvent toSend);

    public default void metaControl(String code) {
        // does nothing
    }
}
