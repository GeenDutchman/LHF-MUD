package com.lhf.interfaces;

import com.lhf.user.UserID;

public interface ConnectionListener {
    void userConnected(UserID id);
    void userLeft(UserID id);
}
