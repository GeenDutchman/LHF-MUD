package com.lhf.messages;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

public interface MessageChainHandler extends ClientMessengerHub {

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
            if (predicate == null) {
                // this.log(Level.FINEST, "No enabling predicate found, thus disabled");
                return false;
            }
            boolean testResult = predicate.test(ctx);
            // this.log(Level.FINEST, () -> String.format("Predicate enabled %b per context:
            // %s", testResult, ctx));
            return testResult;
        }

        public Optional<String> getHelp(CommandContext ctx);

        public Predicate<CommandContext> getEnabledPredicate();

        public CommandContext.Reply handleCommand(CommandContext ctx, Command cmd);

        public MessageChainHandler getChainHandler();

        public default void log(Level logLevel, String logMessage) {
            this.getChainHandler().log(logLevel, logMessage);
        }

        public default void log(Level logLevel, Supplier<String> logMessageSupplier) {
            this.getChainHandler().log(logLevel, logMessageSupplier);
        }

        @Override
        default int compareTo(CommandHandler arg0) {
            return this.getHandleType().compareTo(arg0.getHandleType());
        }
    }

    public abstract Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx);

    public default CommandContext.Reply handle(CommandContext ctx, Command cmd) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        Map<CommandMessage, CommandHandler> handlers = this.getCommands(ctx);
        ctx = MessageChainHandler.addHelps(handlers, ctx);
        if (cmd != null && handlers != null) {
            CommandHandler handler = handlers.get(cmd.getType());
            if (handler == null) {
                this.log(Level.FINEST,
                        () -> String.format("No CommandHandler for type %s at this level", cmd.getType()));
            } else if (handler.isEnabled(ctx)) {
                CommandContext.Reply reply = handler.handleCommand(ctx, cmd);
                if (reply == null) {
                    this.log(Level.SEVERE,
                            () -> String.format("No reply for handler of type %s", handler.getHandleType()));
                } else if (reply.isHandled()) {
                    return reply;
                }
            }
        }
        return ctx.failhandle();
    }

    public default CommandContext.Reply handleChain(CommandContext ctx, Command cmd) {
        CommandContext.Reply thisLevelReply = this.handle(ctx, cmd);
        if (thisLevelReply != null && thisLevelReply.isHandled()) {
            return thisLevelReply;
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
            if (handler != null && handler.isEnabled(ctx)) {
                Optional<String> helpString = handler.getHelp(ctx);
                if (helpString != null && helpString.isPresent()) {
                    ctx.addHelp(handler.getHandleType(), helpString.get());
                }
            }
        }
        return ctx;
    }

    public static CommandContext.Reply passUpChain(MessageChainHandler presentChainHandler, CommandContext ctx,
            Command msg) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (presentChainHandler == null) {
            return ctx.failhandle();
        }
        ctx = presentChainHandler.addSelfToContext(ctx);
        ctx = MessageChainHandler.addHelps(presentChainHandler.getCommands(ctx), ctx);
        MessageChainHandler currentChainHandler = presentChainHandler.getSuccessor();
        while (currentChainHandler != null) {
            CommandContext.Reply thisLevelReply = currentChainHandler.handle(ctx, msg);
            if (thisLevelReply != null && thisLevelReply.isHandled()) {
                return thisLevelReply;
            }
            currentChainHandler = currentChainHandler.getSuccessor();
        }
        presentChainHandler.log(Level.INFO,
                String.format("No successor handled message: %s\n%s", ctx.toString(), ctx.getHelps()));
        return ctx.failhandle();
    }

}
