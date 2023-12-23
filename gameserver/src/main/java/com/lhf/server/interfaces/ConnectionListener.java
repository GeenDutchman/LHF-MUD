package com.lhf.server.interfaces;

import com.lhf.messages.ClientID;

public interface ConnectionListener {
    void clientConnected(ClientID id);

    void clientLeft(ClientID id);

    void clientConnectionTerminated(ClientID id);
}
