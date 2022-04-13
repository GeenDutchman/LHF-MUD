package com.lhf.server.interfaces;

import com.lhf.messages.in.InMessage;
import com.lhf.server.client.user.UserID;

public interface UserListener {
    void userConnected(UserID id);

    void userLeft(UserID id);

    void messageReceived(UserID id, InMessage msg);
}
