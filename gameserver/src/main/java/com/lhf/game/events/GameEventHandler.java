package com.lhf.game.events;

import java.util.Map;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandContext;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.CommandContext.Reply;

public interface GameEventHandler {

    public void setSuccessor(GameEventHandler successor);

    public GameEventHandler getSuccessor();

    public default void intercept(GameEventHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands(CommandContext ctx);

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public default CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        GameEventHandler retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
