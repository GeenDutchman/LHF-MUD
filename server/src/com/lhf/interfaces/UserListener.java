package com.lhf.interfaces;


import com.lhf.messages.in.InMessage;
import com.lhf.user.UserID;

public interface UserListener {
    void userConnected(UserID id);
    void userLeft(UserID id);
    void messageReceived(UserID id, InMessage msg);
}
