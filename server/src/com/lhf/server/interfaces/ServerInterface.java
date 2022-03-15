package com.lhf.server.interfaces;

import com.lhf.server.client.user.UserID;
import com.lhf.server.messages.out.OutMessage;

public interface ServerInterface {
    void registerCallback(UserListener listener);

    boolean sendMessageToUser(OutMessage msg, @NotNull UserID id);

    void sendMessageToAll(OutMessage msg);

    void start();

    void sendMessageToAllExcept(OutMessage msg, UserID id);

    void removeUser(UserID id);
}
