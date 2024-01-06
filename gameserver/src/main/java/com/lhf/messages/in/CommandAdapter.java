package com.lhf.messages.in;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.ICommand;
import com.lhf.messages.grammar.Prepositions;

public abstract class CommandAdapter {
    protected final Command command;

    public CommandAdapter(Command command) {
        this.command = command;
    }

    protected final Command getCommand() {
        return this.command;
    }

    protected String getWhole() {
        return command.getWhole();
    }

    public CommandMessage getType() {
        return command.getType();
    }

    protected List<String> getDirects() {
        return command.getDirects();
    }

    public Boolean isValid() {
        return command.isValid();
    }

    @Deprecated(forRemoval = true)
    public List<String> getWhat() {
        return command.getWhat();
    }

    protected String getByPreposition(Prepositions preposition) {
        return command.getByPreposition(preposition);
    }

    protected Map<Prepositions, String> getIndirects() {
        return command.getIndirects();
    }

    @Override
    public int hashCode() {
        return command.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return command.equals(obj);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Payload:").add(this.getWhole());
        return this.command.toString();
    }

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

    public static Command fromCommand(CommandMessage cmdMsg, String arguments) {
        if (cmdMsg == null) {
            return null;
        }
        switch (cmdMsg) {
            case ATTACK:
                return new AttackMessage(arguments);

            case CAST:
                return new CastMessage(arguments);

            case CREATE:
                return new CreateInMessage(arguments);

            case DROP:
                return new DropMessage(arguments);

            case EQUIP:
                return new EquipMessage(arguments);

            case SEE:
                return new SeeMessage(arguments);

            case EXIT:
                return new ExitMessage(arguments);

            case GO:
                return new GoMessage(arguments);

            case HELP:
                return new HelpInMessage(arguments);

            case INTERACT:
                return new InteractMessage(arguments);

            case INVENTORY:
                return new InventoryMessage(arguments);

            case PLAYERS:
                return new ListPlayersMessage(arguments);
            case SAY:
                return new SayMessage(arguments);

            case SHOUT:
                return new ShoutMessage(arguments);

            case STATUS:
                return new StatusMessage(arguments);

            case TAKE:
                return new TakeMessage(arguments);

            case UNEQUIP:
                return new UnequipMessage(arguments);

            case USE:
                return new UseMessage(arguments);
            case PASS:
                return new PassMessage(arguments);
            case LEWD:
                return new LewdInMessage(arguments);
            case SPELLBOOK:
                return new SpellbookMessage(arguments);
            case STATS:
                return new StatsInMessage(arguments);
            case REPEAT:
                return new RepeatInMessage(arguments);
            case FOLLOW:
                return new FollowMessage(arguments);
            case REST:
                return new RestMessage(arguments);
            default:
                return new HelpInMessage(arguments);

        }
    }

    public static Optional<Command> fromString(String payload) {
        if (payload == null || payload.length() == 0) {
            return Optional.empty();
        }

        String[] words = payload.split(" ");
        Stream<String> stream = Arrays.stream(words);
        CommandMessage matched = CommandMessage.getCommandMessage(words[0]);
        String arguments = stream.skip(1).collect(Collectors.joining(" ")).trim();

        return Optional.of(CommandAdapter.fromCommand(matched, arguments));
    }

}
