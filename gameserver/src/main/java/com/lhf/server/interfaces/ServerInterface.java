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
    default String getColorTaggedName() {
        return this.getStartTag() + "Server" + this.getEndTag();
    }

    @Override
    default String getEndTag() {
        return "</Server>";
    }

    @Override
    default String getStartTag() {
        return "<Server>";
    }

}
