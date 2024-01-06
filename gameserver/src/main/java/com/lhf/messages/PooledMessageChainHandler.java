package com.lhf.messages;

import java.util.Deque;
import java.util.Map;
import java.util.NavigableMap;
import java.util.logging.Level;

import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.in.DropMessage;
import com.lhf.messages.in.EquipMessage;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.in.FollowMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.HelpInMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.InventoryMessage;
import com.lhf.messages.in.LewdInMessage;
import com.lhf.messages.in.ListPlayersMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.in.RepeatInMessage;
import com.lhf.messages.in.RestMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.messages.in.SpellbookMessage;
import com.lhf.messages.in.StatsInMessage;
import com.lhf.messages.in.StatusMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.in.UnequipMessage;
import com.lhf.messages.in.UseMessage;

/**
 * Meant to hold a buffer of {@link com.lhf.messages.Command Command}s for a
 * time.
 * These pooled commands remain until {@link #flush() flushed}.
 * The pools are Keyed by something {@link java.lang.Comparable Comparable} to
 * other Keys.
 */
public interface PooledMessageChainHandler<Key extends Comparable<Key>> extends CommandChainHandler {

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

    public static class FlushableCommandHandlerMetadata extends CommandHandlerMetadata {
        protected boolean empoolEnabled;

        @Override
        public boolean isEmpoolEnabled() {
            return this.empoolEnabled;
        }

    }

    // dispatch
    public default boolean empoolOrHandle(CommandContext ctx, Command cmd) {
        final Map<CommandMessage, ICommandHandlerMetadata> commands = this.getCommands(ctx);
        final ICommandHandlerMetadata metadata = commands.get(cmd.getType());
        return metadata == null ? false : metadata.isEmpoolEnabled();

    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, AttackMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, CastMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, CreateInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, DropMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, EquipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, ExitMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, FollowMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, GoMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, HelpInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, InteractMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, InventoryMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, LewdInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, ListPlayersMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, PassMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, RepeatInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, RestMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, SayMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, SeeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, ShoutMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, SpellbookMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, StatsInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, StatusMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, TakeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, UnequipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    @Override
    public default CommandContext.Reply handleInCommand(CommandContext ctx, UseMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        if (this.empoolOrHandle(ctx, cmd)) {
            this.empool(ctx, cmd);
            return ctx.handled();
        }
        return this.flushHandleInCommand(ctx, cmd);
    }

    // dispatch
    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, Command cmd) {
        ctx = this.addSelfToContext(ctx);
        this.log(Level.SEVERE,
                String.format("Need to add a specific implementation for this command. Type %s, String %s, Context %s",
                        cmd.getClass().getName(), cmd, ctx));
        throw new UnsupportedOperationException(
                String.format("Need to add a specific implementation for this command. Type %s, String %s, Context %s",
                        cmd.getClass().getName(), cmd, ctx));
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, AttackMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, CastMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, CreateInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, DropMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, EquipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, ExitMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, FollowMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, GoMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, HelpInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, InteractMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, InventoryMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, LewdInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, ListPlayersMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, PassMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, RepeatInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, RestMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, SayMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, SeeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, ShoutMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, SpellbookMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, StatsInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, StatusMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, TakeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, UnequipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply flushHandleInCommand(CommandContext ctx, UseMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        return CommandChainHandler.super.handleInCommand(ctx, cmd);
    }
}
