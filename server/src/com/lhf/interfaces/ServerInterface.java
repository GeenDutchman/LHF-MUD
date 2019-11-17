package com.lhf.interfaces;

import com.lhf.messages.in.InMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.user.UserID;
import org.jetbrains.annotations.NotNull;

public interface ServerInterface {
    void registerCallback(UserListener listener);
    boolean sendMessageToUser(OutMessage msg, @NotNull UserID id);
    void sendMessageToAll(OutMessage msg);
    void start();
    void sendMessageToAllExcept(OutMessage msg, UserID id);
    void removeUser(UserID id);
}
