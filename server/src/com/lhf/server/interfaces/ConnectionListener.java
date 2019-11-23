package com.lhf.server.interfaces;


import com.lhf.server.client.ClientID;

public interface ConnectionListener {
    void userConnected(ClientID id);
    void userLeft(ClientID id);
}
