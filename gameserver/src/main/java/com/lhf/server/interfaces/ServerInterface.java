package com.lhf.server.interfaces;

import java.util.function.Predicate;

import com.lhf.messages.CommandContext;
import com.lhf.messages.MessageChainHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends MessageChainHandler {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    public interface ServerCommandHandler extends CommandHandler {
        static final Predicate<CommandContext> alreadyCreatedPredicate = CommandHandler.defaultPredicate
                .and(ctx -> ctx.getUserID() == null);
    }
}
