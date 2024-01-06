package com.lhf.messages.in;

import java.util.Map;
import java.util.logging.Level;

import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.PooledMessageChainHandler;
import com.lhf.messages.CommandChainHandler.CommandHandler;

public class CommandDispatcher {
    public static CommandContext.Reply dispatch(CommandContext ctx, Command command, CommandChainHandler chainHandler) {
        if (ctx == null || command == null || chainHandler == null) {
            throw new IllegalArgumentException(
                    String.format("Cannot use a null context, command, or handler: %s, %s, %s", ctx, command,
                            chainHandler));
        }
        AMessageType commandType = command.getType();
        if (commandType == null) {
            throw new IllegalStateException(String.format("Command '%s' must not have a null type", command));
        }
        chainHandler.addSelfToContext(ctx);
        Map<AMessageType, CommandHandler<?>> handlers = chainHandler.getCommands(ctx);
        if (handlers == null) {
            chainHandler.log(Level.FINEST,
                    () -> String.format("No CommandHandler for type %s at this level", commandType));
            return ctx.failhandle();
        }

        switch (commandType) {
            case ATTACK:
                CommandHandler<?> handler = handlers.get(commandType);
                handler.handleCommand(ctx, command);
                return chainHandler.handleCommand(ctx, new AttackMessage(command));
            case CAST:
                return chainHandler.handleCommand(ctx, new CastMessage(command));
            case CREATE:
                return chainHandler.handleCommand(ctx, new CreateInMessage(command));
            case DROP:
                return chainHandler.handleCommand(ctx, new DropMessage(command));
            case EQUIP:
                return chainHandler.handleCommand(ctx, new EquipMessage(command));
            case EXIT:
                return chainHandler.handleCommand(ctx, new ExitMessage(command));
            case FOLLOW:
                return chainHandler.handleCommand(ctx, new FollowMessage(command));
            case GO:
                return chainHandler.handleCommand(ctx, new GoMessage(command));
            case HELP:
                return chainHandler.handleCommand(ctx, new HelpInMessage(command));
            case INTERACT:
                return chainHandler.handleCommand(ctx, new InteractMessage(command));
            case INVENTORY:
                return chainHandler.handleCommand(ctx, new InventoryMessage(command));
            case LEWD:
                return chainHandler.handleCommand(ctx, new LewdInMessage(command));
            case PASS:
                return chainHandler.handleCommand(ctx, new PassMessage(command));
            case PLAYERS:
                return chainHandler.handleCommand(ctx, new ListPlayersMessage(command));
            case REPEAT:
                return chainHandler.handleCommand(ctx, new RepeatInMessage(command));
            case REST:
                return chainHandler.handleCommand(ctx, new RestMessage(command));
            case SAY:
                return chainHandler.handleCommand(ctx, new SayMessage(command));
            case SEE:
                return chainHandler.handleCommand(ctx, new SeeMessage(command));
            case SHOUT:
                return chainHandler.handleCommand(ctx, new ShoutMessage(command));
            case SPELLBOOK:
                return chainHandler.handleCommand(ctx, new SpellbookMessage(command));
            case STATS:
                return chainHandler.handleCommand(ctx, new StatsInMessage(command));
            case STATUS:
                return chainHandler.handleCommand(ctx, new StatusMessage(command));
            case TAKE:
                return chainHandler.handleCommand(ctx, new TakeMessage(command));
            case UNEQUIP:
                return chainHandler.handleCommand(ctx, new UnequipMessage(command));
            case USE:
                return chainHandler.handleCommand(ctx, new UseMessage(command));
            default:
                throw new UnsupportedOperationException(
                        String.format("Need adapter for command type %s", commandType.toString()));

        }

    }

    public static CommandContext.Reply flushDispatch(CommandContext ctx, Command command,
            PooledMessageChainHandler<?> handler) {
        if (ctx == null || command == null || handler == null) {
            throw new IllegalArgumentException(
                    String.format("Cannot use a null context, command, or handler: %s, %s, %s", ctx, command, handler));
        }
        AMessageType commandType = command.getType();
        if (commandType == null) {
            throw new IllegalStateException(String.format("Command '%s' must not have a null type", command));
        }
        switch (commandType) {
            case ATTACK:
                return handler.flushHandle(ctx, new AttackMessage(command));
            case CAST:
                return handler.flushHandle(ctx, new CastMessage(command));
            case CREATE:
                return handler.flushHandle(ctx, new CreateInMessage(command));
            case DROP:
                return handler.flushHandle(ctx, new DropMessage(command));
            case EQUIP:
                return handler.flushHandle(ctx, new EquipMessage(command));
            case EXIT:
                return handler.flushHandle(ctx, new ExitMessage(command));
            case FOLLOW:
                return handler.flushHandle(ctx, new FollowMessage(command));
            case GO:
                return handler.flushHandle(ctx, new GoMessage(command));
            case HELP:
                return handler.flushHandle(ctx, new HelpInMessage(command));
            case INTERACT:
                return handler.flushHandle(ctx, new InteractMessage(command));
            case INVENTORY:
                return handler.flushHandle(ctx, new InventoryMessage(command));
            case LEWD:
                return handler.flushHandle(ctx, new LewdInMessage(command));
            case PASS:
                return handler.flushHandle(ctx, new PassMessage(command));
            case PLAYERS:
                return handler.flushHandle(ctx, new ListPlayersMessage(command));
            case REPEAT:
                return handler.flushHandle(ctx, new RepeatInMessage(command));
            case REST:
                return handler.flushHandle(ctx, new RestMessage(command));
            case SAY:
                return handler.flushHandle(ctx, new SayMessage(command));
            case SEE:
                return handler.flushHandle(ctx, new SeeMessage(command));
            case SHOUT:
                return handler.flushHandle(ctx, new ShoutMessage(command));
            case SPELLBOOK:
                return handler.flushHandle(ctx, new SpellbookMessage(command));
            case STATS:
                return handler.flushHandle(ctx, new StatsInMessage(command));
            case STATUS:
                return handler.flushHandle(ctx, new StatusMessage(command));
            case TAKE:
                return handler.flushHandle(ctx, new TakeMessage(command));
            case UNEQUIP:
                return handler.flushHandle(ctx, new UnequipMessage(command));
            case USE:
                return handler.flushHandle(ctx, new UseMessage(command));
            default:
                throw new UnsupportedOperationException(
                        String.format("Need adapter for command type %s", commandType.toString()));

        }

    }
}
