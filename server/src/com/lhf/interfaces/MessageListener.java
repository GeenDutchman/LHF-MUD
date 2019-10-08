package com.lhf.interfaces;

import com.lhf.messages.UserMessage;
import com.lhf.user.UserID;

public interface MessageListener {
    void messageReceived(UserID id, UserMessage msg);
}
