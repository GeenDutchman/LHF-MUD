package com.lhf.server.interfaces;

import java.util.EnumMap;
import java.util.Map;

import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends MessageHandler {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    @Override
    default Map<CommandMessage, String> getCommands(CommandContext ctx) {
        EnumMap<CommandMessage, String> gathered = new EnumMap<>(CommandMessage.class);
        if (ctx.getUser() != null) {
            gathered.remove(CommandMessage.CREATE);
        }
        return ctx.addHelps(gathered);
    }
}
