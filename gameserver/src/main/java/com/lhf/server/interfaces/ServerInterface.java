package com.lhf.server.interfaces;

import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends CommandChainHandler {
    void registerCallback(UserListener listener);

    void unregisterCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    public interface ServerCommandHandler extends CommandHandler {
    }

    @Override
    default String getTagName() {
        return "Server";
    }

    @Override
    default String getSimpleContent() {
        return this.getClass().getSimpleName();
    }

}
