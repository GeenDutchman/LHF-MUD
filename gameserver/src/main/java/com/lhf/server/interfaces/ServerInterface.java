package com.lhf.server.interfaces;

import com.lhf.messages.MessageHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends MessageHandler {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);
}
