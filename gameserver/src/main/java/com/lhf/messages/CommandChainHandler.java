package com.lhf.messages;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.in.AMessageType;

public interface CommandChainHandler extends GameEventProcessorHub {

    public void setSuccessor(CommandChainHandler successor);

    public CommandChainHandler getSuccessor();

    public default void intercept(CommandChainHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    /**
     * An interface meant to handle commands, with the issue of who
     * it is taking care
     * of commands *for* retrieved from the context to allow for static classes.
     */
    public interface CommandHandler extends Comparable<CommandHandler> {

        /**
         * Adapt the command to the type of lens we expect
         * 
         * @param command
         * @return
         */
        // public abstract CommandAdapter adaptCommand(Command command);

        /**
         * Gets what type of command we're meant to handle
         * 
         * @return
         */
        public abstract AMessageType getHandleType();

        /**
         * Checks to see if the CommandHandler is enabled per the context
         * 
         * @param ctx
         * @return
         */
        public abstract boolean isEnabled(CommandContext ctx);

        /**
         * Gets an optional String of help about this command
         * 
         * @param ctx
         * @return
         */
        public abstract Optional<String> getHelp(CommandContext ctx);

        /**
         * Handles a Command by internally adapting it to the expected shape.
         * <p>
         * 
         * @throws IllegalArgumentException if the adaptation results in a null
         * @param ctx
         * @param command
         * @return reply.handled() if it was handled, reply.failHandle() if it isn't our
         *         problem
         */
        public abstract CommandContext.Reply handleCommand(CommandContext ctx, Command command);

        /**
         * Gets the chainHandler that we want to deal with from the context
         * 
         * @param ctx
         * @return
         */
        public abstract CommandChainHandler getChainHandler(CommandContext ctx);

        public default void log(Level logLevel, String logMessage) {
            Logger.getLogger(this.getClass().getName()).log(logLevel, logMessage);
        }

        public default void log(Level logLevel, Supplier<String> logMessageSupplier) {
            Logger.getLogger(this.getClass().getName()).log(logLevel, logMessageSupplier);
        }

        @Override
        public default int compareTo(CommandHandler arg0) {
            return this.getHandleType().compareTo(arg0.getHandleType());
        }

    }

    public abstract Map<AMessageType, CommandHandler> getCommands(CommandContext ctx);

    public default CommandContext.Reply handle(CommandContext ctx, Command cmd) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        Map<AMessageType, CommandHandler> handlers = this.getCommands(ctx);

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
        ctx = CommandChainHandler.addHelps(handlers, ctx);
        return ctx.failhandle();
    }

    public default CommandContext.Reply handleChain(CommandContext ctx, Command cmd) {
        CommandContext.Reply thisLevelReply = this.handle(ctx, cmd);
        if (thisLevelReply != null && thisLevelReply.isHandled()) {
            return thisLevelReply;
        }
        return CommandChainHandler.passUpChain(this, ctx, cmd);
    }

    private static CommandContext addHelps(Map<AMessageType, CommandHandler> handlers, CommandContext ctx) {
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

    public static CommandContext.Reply passUpChain(final CommandChainHandler presentChainHandler, CommandContext ctx,
            Command msg) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (presentChainHandler == null) {
            return ctx.failhandle();
        }
        ctx = presentChainHandler.addSelfToContext(ctx);

        CommandChainHandler currentChainHandler = presentChainHandler.getSuccessor();
        while (currentChainHandler != null) {
            CommandContext.Reply thisLevelReply = currentChainHandler.handle(ctx, msg);
            if (thisLevelReply != null && thisLevelReply.isHandled()) {
                return thisLevelReply;
            }
            currentChainHandler = currentChainHandler.getSuccessor();
        }

        ctx = CommandChainHandler.addHelps(presentChainHandler.getCommands(ctx), ctx);
        presentChainHandler.log(Level.INFO,
                String.format("No successor handled message: %s\n%s", ctx.toString(), ctx.getHelps()));
        return ctx.failhandle();
    }

}
