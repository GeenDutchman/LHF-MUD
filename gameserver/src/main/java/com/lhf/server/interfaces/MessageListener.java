package com.lhf.server.interfaces;

import com.lhf.messages.in.InMessage;
import com.lhf.server.client.ClientID;

public interface MessageListener {
    void messageReceived(ClientID id, InMessage msg);
}
