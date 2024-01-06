package com.lhf.messages.in;

import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.PooledMessageChainHandler;

public class CommandDispatcher {
    public static CommandContext.Reply dispatch(CommandContext ctx, Command command, CommandChainHandler handler) {
        if (ctx == null || command == null || handler == null) {
            throw new IllegalArgumentException(
                    String.format("Cannot use a null context, command, or handler: %s, %s, %s", ctx, command, handler));
        }
        CommandMessage commandType = command.getType();
        if (commandType == null) {
            throw new IllegalStateException(String.format("Command '%s' must not have a null type", command));
        }
        switch (commandType) {
            case ATTACK:
                return handler.handleInCommand(ctx, new AttackMessage(command));
            case CAST:
                return handler.handleInCommand(ctx, new CastMessage(command));
            case CREATE:
                return handler.handleInCommand(ctx, new CreateInMessage(command));
            case DROP:
                return handler.handleInCommand(ctx, new DropMessage(command));
            case EQUIP:
                return handler.handleInCommand(ctx, new EquipMessage(command));
            case EXIT:
                return handler.handleInCommand(ctx, new ExitMessage(command));
            case FOLLOW:
                return handler.handleInCommand(ctx, new FollowMessage(command));
            case GO:
                return handler.handleInCommand(ctx, new GoMessage(command));
            case HELP:
                return handler.handleInCommand(ctx, new HelpInMessage(command));
            case INTERACT:
                return handler.handleInCommand(ctx, new InteractMessage(command));
            case INVENTORY:
                return handler.handleInCommand(ctx, new InventoryMessage(command));
            case LEWD:
                return handler.handleInCommand(ctx, new LewdInMessage(command));
            case PASS:
                return handler.handleInCommand(ctx, new PassMessage(command));
            case PLAYERS:
                return handler.handleInCommand(ctx, new ListPlayersMessage(command));
            case REPEAT:
                return handler.handleInCommand(ctx, new RepeatInMessage(command));
            case REST:
                return handler.handleInCommand(ctx, new RestMessage(command));
            case SAY:
                return handler.handleInCommand(ctx, new SayMessage(command));
            case SEE:
                return handler.handleInCommand(ctx, new SeeMessage(command));
            case SHOUT:
                return handler.handleInCommand(ctx, new ShoutMessage(command));
            case SPELLBOOK:
                return handler.handleInCommand(ctx, new SpellbookMessage(command));
            case STATS:
                return handler.handleInCommand(ctx, new StatsInMessage(command));
            case STATUS:
                return handler.handleInCommand(ctx, new StatusMessage(command));
            case TAKE:
                return handler.handleInCommand(ctx, new TakeMessage(command));
            case UNEQUIP:
                return handler.handleInCommand(ctx, new UnequipMessage(command));
            case USE:
                return handler.handleInCommand(ctx, new UseMessage(command));
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
        CommandMessage commandType = command.getType();
        if (commandType == null) {
            throw new IllegalStateException(String.format("Command '%s' must not have a null type", command));
        }
        switch (commandType) {
            case ATTACK:
                return handler.flushHandleInCommand(ctx, new AttackMessage(command));
            case CAST:
                return handler.flushHandleInCommand(ctx, new CastMessage(command));
            case CREATE:
                return handler.flushHandleInCommand(ctx, new CreateInMessage(command));
            case DROP:
                return handler.flushHandleInCommand(ctx, new DropMessage(command));
            case EQUIP:
                return handler.flushHandleInCommand(ctx, new EquipMessage(command));
            case EXIT:
                return handler.flushHandleInCommand(ctx, new ExitMessage(command));
            case FOLLOW:
                return handler.flushHandleInCommand(ctx, new FollowMessage(command));
            case GO:
                return handler.flushHandleInCommand(ctx, new GoMessage(command));
            case HELP:
                return handler.flushHandleInCommand(ctx, new HelpInMessage(command));
            case INTERACT:
                return handler.flushHandleInCommand(ctx, new InteractMessage(command));
            case INVENTORY:
                return handler.flushHandleInCommand(ctx, new InventoryMessage(command));
            case LEWD:
                return handler.flushHandleInCommand(ctx, new LewdInMessage(command));
            case PASS:
                return handler.flushHandleInCommand(ctx, new PassMessage(command));
            case PLAYERS:
                return handler.flushHandleInCommand(ctx, new ListPlayersMessage(command));
            case REPEAT:
                return handler.flushHandleInCommand(ctx, new RepeatInMessage(command));
            case REST:
                return handler.flushHandleInCommand(ctx, new RestMessage(command));
            case SAY:
                return handler.flushHandleInCommand(ctx, new SayMessage(command));
            case SEE:
                return handler.flushHandleInCommand(ctx, new SeeMessage(command));
            case SHOUT:
                return handler.flushHandleInCommand(ctx, new ShoutMessage(command));
            case SPELLBOOK:
                return handler.flushHandleInCommand(ctx, new SpellbookMessage(command));
            case STATS:
                return handler.flushHandleInCommand(ctx, new StatsInMessage(command));
            case STATUS:
                return handler.flushHandleInCommand(ctx, new StatusMessage(command));
            case TAKE:
                return handler.flushHandleInCommand(ctx, new TakeMessage(command));
            case UNEQUIP:
                return handler.flushHandleInCommand(ctx, new UnequipMessage(command));
            case USE:
                return handler.flushHandleInCommand(ctx, new UseMessage(command));
            default:
                throw new UnsupportedOperationException(
                        String.format("Need adapter for command type %s", commandType.toString()));

        }

    }
}
