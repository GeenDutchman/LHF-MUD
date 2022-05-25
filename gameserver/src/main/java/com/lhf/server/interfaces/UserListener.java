package com.lhf.server.interfaces;

import com.lhf.server.client.user.UserID;

public interface UserListener {
    void userConnected(UserID id);

    void userLeft(UserID id);
}
