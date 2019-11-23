package com.lhf.server.interfaces;


import com.lhf.server.client.user.UserID;
import com.lhf.server.messages.in.InMessage;

public interface UserListener {
    void userConnected(UserID id);
    void userLeft(UserID id);
    void messageReceived(UserID id, InMessage msg);
}
