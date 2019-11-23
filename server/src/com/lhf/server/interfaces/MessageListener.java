package com.lhf.server.interfaces;

import com.lhf.server.client.ClientID;
import com.lhf.server.messages.in.InMessage;

public interface MessageListener {
    void messageReceived(ClientID id, InMessage msg);
}
