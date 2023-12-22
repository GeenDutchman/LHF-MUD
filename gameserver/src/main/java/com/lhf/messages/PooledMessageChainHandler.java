package com.lhf.messages;

import java.util.Deque;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.lhf.messages.CommandContext.Reply;

/**
 * Meant to hold a buffer of {@link com.lhf.messages.Command Command}s for a
 * time.
 * These pooled commands remain until {@link #flush() flushed}.
 * The pools are Keyed by something {@link java.lang.Comparable Comparable} to
 * other Keys.
 */
public interface PooledMessageChainHandler<Key extends Comparable<Key>> extends MessageChainHandler {

    /**
     * General interface for a Pooled {@link com.lhf.messages.Command Command}
     */
    public interface IPoolEntry {

        /**
         * 
         * @return the {@link com.lhf.messages.CommandContext Context} related to the
         *         {@link com.lhf.messages.Command Command}
         */
        public CommandContext getContext();

        /**
         * 
         * @return the pooled {@link com.lhf.messages.Command Command} itself
         */
        public Command getCommand();
    }

    /**
     * Default concretion of a
     * {@link com.lhf.messages.PooledMessageChainHandler.IPoolEntry IPoolEntry}.
     * 
     * Contains a {@link com.lhf.messages.CommandContext Context} and
     * {@link com.lhf.messages.Command Command}.
     */
    public class PoolEntry implements IPoolEntry {
        private final CommandContext ctx;
        private final Command command;

        public PoolEntry(CommandContext ctx, Command command) {
            this.ctx = ctx;
            this.command = command;
        }

        @Override
        public CommandContext getContext() {
            return ctx;
        }

        @Override
        public Command getCommand() {
            return command;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PoolEntry [ctx=").append(ctx).append(", command=").append(command).append("]");
            return builder.toString();
        }

    }

    /**
     * Returns the pools for each Key.
     * 
     * @return map of pools
     */
    public NavigableMap<Key, Deque<IPoolEntry>> getPools();

    /**
     * Attempts to place entry into pool
     * 
     * @param key
     * @param entry
     * @return true if successfully emplaced, false otherwise
     */
    public boolean empool(Key key, IPoolEntry entry);

    /**
     * Constructs a {@link com.lhf.messages.PooledMessageChainHandler.PoolEntry
     * PoolEntry} and attempts to place it into pool
     * 
     * @param key
     * @param ctx
     * @param cmd
     * @return true if successfully emplaced, false otherwise
     */
    public default boolean empool(Key key, CommandContext ctx, Command cmd) {
        if (key == null || cmd == null) {
            this.log(Level.WARNING, "cannot empool with null key or command");
            return false;
        }
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        return this.empool(key, new PoolEntry(ctx, cmd));
    }

    /**
     * Constructs a {@link com.lhf.messages.PooledMessageChainHandler.PoolEntry
     * PoolEntry} and attempts to place it into pool
     * 
     * @param ctx
     * @param cmd
     * @return true if successfully emplaced, false otherwise
     */
    public default boolean empool(CommandContext ctx, Command cmd) {
        if (cmd == null) {
            this.log(Level.WARNING, "cannot empool with null command");
            return false;
        }
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        Key key = this.keyFromContext(ctx);
        if (key == null) {
            this.log(Level.WARNING, String.format("key is null and cannot empool, generated from %s", ctx));
            return false;
        }
        return this.empool(key, new PoolEntry(ctx, cmd));
    }

    /**
     * Attempts to generate a Key from the {@link com.lhf.messages.CommandContext
     * Context}
     * 
     * @param ctx
     * @return a Key if successful, null if not
     */
    public Key keyFromContext(CommandContext ctx);

    /**
     * Checks if this key's pool is ready to flush.
     * 
     * @param key
     * @return
     */
    public boolean isReadyToFlush(Key key);

    /**
     * Check if the all the pools are ready to flush. Should have no side effects.
     * 
     * @return true if ready, false if not
     */
    public default boolean isReadyToFlush() {
        NavigableMap<Key, Deque<IPoolEntry>> pools = this.getPools();
        if (pools == null) {
            return true;
        }
        return pools.keySet().stream().allMatch(key -> this.isReadyToFlush(key));
    }

    /**
     * Goes through all the pools and handles each Command.
     */
    public void flush();

    /**
     * Used in conjuction with the {@link com.lhf.messages.PooledMessageChainHandler
     * PooledMessageChainHandler} to pool {@link com.lhf.messages.Command Command}s.
     * If a PooledCommandHandler is in a normal
     * {@link com.lhf.messages.MessageChainHandler MessageChainHandler} then it will
     * do nothing special.
     */
    public interface PooledCommandHandler extends MessageChainHandler.CommandHandler {

        /**
         * Said predicate should check if the {@link com.lhf.messages.CommandContext
         * Context} allows for pooling.
         * 
         * @return Predicate to check for if the command can be pooled.
         */
        public Predicate<CommandContext> getPoolingPredicate();

        /**
         * Retrieves {@link #getPoolingPredicate()} and tests it, if present.
         * 
         * @param ctx
         * @return true when the predicate is retrieved and tests positive, false
         *         otherwise
         */
        public default boolean isPoolingEnabled(CommandContext ctx) {
            Predicate<CommandContext> predicate = this.getPoolingPredicate();
            if (predicate == null) {
                // this.log(Level.FINEST, "No pooling predicate found, thus disabled");
                return false;
            }
            boolean testResult = predicate.test(ctx);
            // this.log(Level.FINEST, () -> String.format("Pooling enabled %b per context:
            // %s", testResult, ctx));
            return testResult;
        }

        /**
         * An overrideable method for when the empooling happens
         * 
         * @param ctx
         * @param empoolResult
         * @return
         */
        public default boolean onEmpool(CommandContext ctx, boolean empoolResult) {
            this.log(Level.FINEST, () -> String.format("Empooling %b for context %s", empoolResult, ctx));
            return empoolResult;
        }

        /**
         * If the result of {@link #isPoolingEnabled(CommandContext)} is true and a
         * {@link com.lhf.messages.PooledMessageChainHandler PooledMessageChainHandler}
         * is available, then it
         * will call {@link #empool(CommandContext, Command)} and return a handled
         * reply.
         * Otherwise, then it will call
         * {@link #flushHandle(CommandContext, Command)} which should hold the actual
         * logic for processing the command and return that resulting reply.
         * 
         * @param ctx
         * @param cmd
         * @return a Reply
         */
        @Override
        default Reply handleCommand(CommandContext ctx, Command cmd) {
            if (this.isPoolingEnabled(ctx)) {
                PooledMessageChainHandler<?> pooledChainHandler = this.getPooledChainHandler(ctx);
                if (pooledChainHandler != null) {
                    this.onEmpool(ctx, pooledChainHandler.empool(ctx, cmd));
                    return ctx.handled();
                }
                this.log(Level.FINE, () -> String.format("No PooledChainHandler available per context: %s", ctx));
            }
            this.log(Level.FINE, "Proceeding from handle() -> flushHandle()");
            return this.flushHandle(ctx, cmd);
        }

        /**
         * Holds the actual logic for handling the command with its context.
         * 
         * @param ctx
         * @param cmd
         * @return
         */
        public CommandContext.Reply flushHandle(CommandContext ctx, Command cmd);

        /**
         * Retrieves the PooledChainHandler, potentially using the context
         */
        public PooledMessageChainHandler<?> getPooledChainHandler(CommandContext ctx);

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

    public default Reply flushHandle(CommandContext ctx, Command cmd) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        ctx = this.addSelfToContext(ctx);
        Map<CommandMessage, CommandHandler> handlers = this.getCommands(ctx);
        ctx = PooledMessageChainHandler.addHelps(handlers, ctx);
        if (cmd != null && handlers != null) {
            CommandHandler handler = handlers.get(cmd.getType());
            if (handler == null) {
                this.log(Level.FINEST,
                        () -> String.format("No CommandHandler for type %s at this level", cmd.getType()));
            } else if (handler.isEnabled(ctx)) {
                CommandContext.Reply reply = null;
                if (handler instanceof PooledCommandHandler pooledCommandHandler) {
                    reply = pooledCommandHandler.flushHandle(ctx, cmd);
                } else {
                    reply = handler.handleCommand(ctx, cmd);
                }
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

    default Reply handleFlushChain(CommandContext ctx, Command cmd) {
        CommandContext.Reply thisLevelReply = this.flushHandle(ctx, cmd);
        if (thisLevelReply != null && thisLevelReply.isHandled()) {
            return thisLevelReply;
        }
        return PooledMessageChainHandler.flushUpChain(this, ctx, cmd);
    }

    public static CommandContext.Reply flushUpChain(MessageChainHandler presentChainHandler, CommandContext ctx,
            Command msg) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (presentChainHandler == null) {
            return ctx.failhandle();
        }
        ctx = presentChainHandler.addSelfToContext(ctx);
        ctx = PooledMessageChainHandler.addHelps(presentChainHandler.getCommands(ctx), ctx);
        MessageChainHandler currentChainHandler = presentChainHandler.getSuccessor();
        while (currentChainHandler != null) {
            CommandContext.Reply thisLevelReply = null;
            if (currentChainHandler instanceof PooledMessageChainHandler<?> pooledChainHandler) {
                thisLevelReply = pooledChainHandler.flushHandle(ctx, msg);
            } else {
                thisLevelReply = currentChainHandler.handle(ctx, msg);
            }
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
