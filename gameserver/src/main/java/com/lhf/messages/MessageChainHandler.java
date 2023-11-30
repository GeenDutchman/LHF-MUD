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
        final static Predicate<CommandContext> defaultPredicate = (ctx) -> ctx != null;

        public CommandMessage getHandleType();

        public default boolean isEnabled(CommandContext ctx) {
            Predicate<CommandContext> predicate = this.getEnabledPredicate();
            return predicate == null ? false : predicate.test(ctx);
        }

        public Optional<String> getHelp(CommandContext ctx);

        public Predicate<CommandContext> getEnabledPredicate();

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
        ctx = this.addSelfToContext(ctx);
        Map<CommandMessage, CommandHandler> handlers = this.getCommands(ctx);
        ctx = MessageChainHandler.addHelps(handlers, ctx);
        if (cmd != null && handlers != null) {
            CommandHandler handler = handlers.get(cmd.getType());
            if (handler != null && handler.isEnabled(ctx)) {
                CommandContext.Reply reply = handler.handle(ctx, cmd);
                if (reply.isHandled()) {
                    return reply;
                }
            }
        }
        return MessageChainHandler.passUpChain(this, ctx, cmd);
    }

    private static CommandContext addHelps(Map<CommandMessage, CommandHandler> handlers, CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (handlers == null) {
            return ctx;
        }
        for (CommandHandler handler : handlers.values()) {
            if (handler.isEnabled(ctx)) {
                Optional<String> helpString = handler.getHelp(ctx);
                if (helpString.isPresent()) {
                    ctx.addHelp(handler.getHandleType(), helpString.get());
                }
            }
        }
        return ctx;
    }

    public static CommandContext.Reply passUpChain(MessageChainHandler presentChainHandler, CommandContext ctx,
            Command msg) {
        if (presentChainHandler == null) {
            return ctx.failhandle();
        }
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = presentChainHandler.addSelfToContext(ctx);
        ctx = MessageChainHandler.addHelps(presentChainHandler.getCommands(ctx), ctx);
        MessageChainHandler currentChainHandler = presentChainHandler.getSuccessor();
        while (currentChainHandler != null) {
            ctx = currentChainHandler.addSelfToContext(ctx);
            Map<CommandMessage, CommandHandler> successorHandlers = currentChainHandler.getCommands(ctx);
            ctx = MessageChainHandler.addHelps(successorHandlers, ctx);
            if (msg != null && successorHandlers != null) {
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
