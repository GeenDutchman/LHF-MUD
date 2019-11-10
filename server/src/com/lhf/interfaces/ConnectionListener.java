package com.lhf.interfaces;


import com.lhf.server.ClientID;

public interface ConnectionListener {
    void userConnected(ClientID id);
    void userLeft(ClientID id);
}
