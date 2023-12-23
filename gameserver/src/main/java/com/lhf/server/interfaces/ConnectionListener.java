package com.lhf.server.interfaces;

import com.lhf.server.client.Client.ClientID;

public interface ConnectionListener {
    void clientConnected(ClientID id);

    void clientLeft(ClientID id);

    void clientConnectionTerminated(ClientID id);
}
