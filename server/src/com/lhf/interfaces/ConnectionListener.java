package com.lhf.interfaces;

import com.lhf.user.UserID;
import com.lhf.messages.UserMessage;

public interface ConnectionListener {
    void userConnected(UserID id);
    void userLeft(UserID id);
}
