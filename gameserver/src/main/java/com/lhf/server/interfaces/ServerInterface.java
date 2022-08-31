package com.lhf.server.interfaces;

import java.util.EnumMap;

import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends MessageHandler {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    @Override
    default EnumMap<CommandMessage, String> gatherHelp(CommandContext ctx) {
        EnumMap<CommandMessage, String> gathered = MessageHandler.super.gatherHelp(ctx);
        if (ctx.getClientID() != null) {
            gathered.remove(CommandMessage.CREATE);
        }
        return gathered;
    }
}
