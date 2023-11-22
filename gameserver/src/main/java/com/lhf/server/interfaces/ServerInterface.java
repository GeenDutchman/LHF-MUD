package com.lhf.server.interfaces;

import java.util.EnumMap;
import java.util.Map;

import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandlerNode;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends GameEventHandlerNode {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    @Override
    default Map<CommandMessage, String> getHandlers(GameEventContext ctx) {
        EnumMap<CommandMessage, String> gathered = new EnumMap<>(CommandMessage.class);
        if (ctx.getUser() != null) {
            gathered.remove(CommandMessage.CREATE);
        }
        return ctx.addHelps(gathered);
    }
}
