package com.lhf.messages.in;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class InMessage {
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

        return Optional.of(InMessage.fromCommand(matched, arguments));
    }

}
