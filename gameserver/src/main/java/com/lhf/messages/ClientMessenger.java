package com.lhf.messages;

import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.ClientID;

public interface ClientMessenger {
    public void sendMsg(OutMessage msg);

    public ClientID getClientID();
}
