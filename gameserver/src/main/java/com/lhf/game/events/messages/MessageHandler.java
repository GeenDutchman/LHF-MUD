package com.lhf.game.events.messages;

import java.util.Map;

public interface MessageHandler {

    public void setSuccessor(MessageHandler successor);

    public MessageHandler getSuccessor();

    public default void intercept(MessageHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands(CommandContext ctx);

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public default CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        MessageHandler retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
