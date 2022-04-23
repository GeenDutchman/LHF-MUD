package com.lhf.server.interfaces;

import com.lhf.messages.Command;
import com.lhf.server.client.ClientID;

public interface MessageListener {
    void messageReceived(ClientID id, Command msg);
}
