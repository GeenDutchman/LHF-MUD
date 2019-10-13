package com.lhf.interfaces;

import com.lhf.messages.UserMessage;
import com.lhf.user.UserID;
import org.jetbrains.annotations.NotNull;

public interface ServerInterface {
    void registerCallback(ConnectionListener listener);
    void registerCallback(MessageListener listener);
    void sendMessageToUser(UserMessage msg, @NotNull UserID id);
    void sendMessageToAll(UserMessage msg);
    void start();
    void sendMessageToAllExcept(UserMessage msg, UserID id);
    void removeUser(UserID id);
}
