package com.lhf.messages;

import java.util.Map;

public interface MessageChainHandler {

    public void setSuccessor(MessageChainHandler successor);

    public MessageChainHandler getSuccessor();

    public default void intercept(MessageChainHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract Map<CommandMessage, String> getCommands(CommandContext ctx);

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public default CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        MessageChainHandler retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
