package com.lhf.game.events;

import java.util.Map;

import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;

public interface GameEventHandler {

    public void setSuccessor(GameEventHandler successor);

    public GameEventHandler getSuccessor();

    public default void intercept(GameEventHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands(GameEventContext ctx);

    public abstract GameEventContext addSelfToContext(GameEventContext ctx);

    public default GameEventContext.Reply handleMessage(GameEventContext ctx, Command msg) {
        GameEventHandler retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
