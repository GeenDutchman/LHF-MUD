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

        public MessageChainHandler getChainHandler();

        @Override
        default int compareTo(CommandHandler arg0) {
            return this.getHandleType().compareTo(arg0.getHandleType());
        }
    }

    public abstract Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx);

    // public default CommandContext.Reply handleMessage(CommandContext ctx, Command
    // msg) {
    // MessageChainHandler retrievedSuccessor = this.getSuccessor();
    // if (retrievedSuccessor != null) {
    // return retrievedSuccessor.handleMessage(ctx, msg);
    // }
    // return ctx.failhandle();
    // }

    public default CommandContext.Reply handleChain(CommandContext ctx, Command cmd) {
        return MessageChainHandler.passUpChain(this, ctx, cmd);
    }

    public static CommandContext.Reply passUpChain(MessageChainHandler presentChainHandler, CommandContext ctx,
            Command msg) {
        if (presentChainHandler == null) {
            return ctx.failhandle();
        }
        if (ctx == null) {
            ctx = new CommandContext();
        }
        presentChainHandler.addSelfToContext(ctx);
        MessageChainHandler currentChainHandler = presentChainHandler; // keep some track of our start point
        while (currentChainHandler != null) {
            currentChainHandler.addSelfToContext(ctx);
            Map<CommandMessage, CommandHandler> successorHandlers = currentChainHandler.getCommands(ctx);
            for (CommandHandler handler : successorHandlers.values()) {
                if (handler.isEnabled(ctx)) {
                    Optional<String> helpString = handler.getHelp(ctx);
                    if (helpString.isPresent()) {
                        ctx.addHelp(handler.getHandleType(), helpString.get());
                    }
                }
            }
            if (msg != null) {
                CommandHandler handler = successorHandlers.get(msg.getType());
                if (handler != null && handler.isEnabled(ctx)) {
                    CommandContext.Reply reply = handler.handle(ctx, msg);
                    if (reply.isHandled()) {
                        return reply;
                    }
                }
            }
            currentChainHandler = currentChainHandler.getSuccessor();
        }
        return ctx.failhandle();
    }

}
