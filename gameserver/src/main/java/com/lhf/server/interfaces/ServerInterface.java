package com.lhf.server.interfaces;

import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.user.UserID;

public interface ServerInterface {
    void registerCallback(UserListener listener);

    boolean sendMessageToUser(OutMessage msg, @NotNull UserID id);

    void sendMessageToAll(OutMessage msg);

    void start();

    void sendMessageToAllExcept(OutMessage msg, UserID id);

    void removeUser(UserID id);
}
