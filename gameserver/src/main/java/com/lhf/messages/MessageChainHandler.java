package com.lhf.messages;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public interface MessageChainHandler {

    public void setSuccessor(MessageChainHandler successor);

    public MessageChainHandler getSuccessor();

    public default void intercept(MessageChainHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public interface CommandHandler extends Comparable<CommandHandler> {
        public CommandMessage getHandleType();

        public boolean isEnabled(CommandContext ctx);

        public Optional<String> getHelp(CommandContext ctx);

        public Predicate<CommandContext> getEnabledPredicate();

        public boolean setEnabledPredicate(Predicate<CommandContext> predicate);

        public CommandContext.Reply handle(CommandContext ctx, Command cmd);

        @Override
        default int compareTo(CommandHandler arg0) {
            return this.getHandleType().compareTo(arg0.getHandleType());
        }
    }

    public abstract Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx);

    public default CommandContext.Reply handleMessage(CommandContext ctx, Command msg) {
        MessageChainHandler retrievedSuccessor = this.getSuccessor();
        if (retrievedSuccessor != null) {
            return retrievedSuccessor.handleMessage(ctx, msg);
        }
        return ctx.failhandle();
    }

}
