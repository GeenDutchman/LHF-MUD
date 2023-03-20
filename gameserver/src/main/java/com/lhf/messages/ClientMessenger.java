package com.lhf.messages;

import com.lhf.Taggable;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public interface ClientMessenger extends Taggable {
    public void sendMsg(OutMessage msg);

    public default void sendMsg(OutMessage.Builder<?> builder) {
        this.sendMsg(builder.Build());
    }

    public ClientID getClientID();
}
