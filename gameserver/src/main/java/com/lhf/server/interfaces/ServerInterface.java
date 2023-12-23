package com.lhf.server.interfaces;

import java.util.function.Predicate;

import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.user.UserID;

public interface ServerInterface extends CommandChainHandler {
    void registerCallback(UserListener listener);

    void start();

    void removeUser(UserID id);

    public interface ServerCommandHandler extends CommandHandler {
        static final Predicate<CommandContext> alreadyCreatedPredicate = CommandHandler.defaultPredicate
                .and(ctx -> ctx.getUserID() == null);
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
