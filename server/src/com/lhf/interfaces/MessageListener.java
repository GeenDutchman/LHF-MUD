package com.lhf.interfaces;

import com.lhf.messages.in.InMessage;
import com.lhf.server.ClientID;

public interface MessageListener {
    void messageReceived(ClientID id, InMessage msg);
}
