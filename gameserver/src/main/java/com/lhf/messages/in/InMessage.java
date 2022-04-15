package com.lhf.messages.in;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.messages.CommandMessage;

public abstract class InMessage {
    public abstract CommandMessage getType();

    public static Optional<InMessage> fromString(String payload) {
        String[] words = payload.split(" ");
        Stream<String> stream = Arrays.stream(words);
        Optional<String> first = Optional.ofNullable(words[0]);
        String arguments = stream.skip(1).collect(Collectors.joining(" ")).trim();
        return first.flatMap(val -> {
            Optional<InMessage> toReturn = Optional.empty();
            try {
                // allows empty
                switch (val.toLowerCase()) {
                    case "say":
                        toReturn = Optional.of(new SayMessage(arguments));
                        break;
                    case "exit":
                        toReturn = Optional.of(new ExitMessage());
                        break;
                    case "look":
                        toReturn = Optional.of(new LookMessage());
                        break;
                    case "inventory":
                        toReturn = Optional.of(new InventoryMessage());
                        break;
                    case "status":
                        toReturn = Optional.of(new StatusMessage());
                        break;
                    case "players":
                        toReturn = Optional.of(new ListPlayersMessage());
                        break;
                }

                if (toReturn.isEmpty() && arguments.length() > 0) {
                    switch (val.toLowerCase()) {
                        case "create":
                            CreateInMessage create_message = new CreateInMessage(arguments);
                            if (!create_message.getUsername().equals("")) {
                                return Optional.of(create_message);
                            } else {
                                return Optional.empty();
                            }
                        case "examine":
                            return Optional.of(new ExamineMessage(arguments));
                        case "go":
                            return Optional.of(new GoMessage(arguments));
                        case "interact":
                            return Optional.of(new InteractMessage(arguments));
                        case "take":
                            return Optional.of(new TakeMessage(arguments));
                        case "drop":
                            return Optional.of(new DropMessage(arguments));
                        case "equip":
                            return Optional.of(new EquipMessage(arguments));
                        case "unequip":
                            return Optional.of(new UnequipMessage(arguments));
                        case "attack":
                            return Optional.of(new AttackMessage(arguments));
                        case "use":
                            return Optional.of(new UseMessage(arguments));
                        case "cast":
                            return Optional.of(new CastMessage(arguments));
                        default:
                            return Optional.empty();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
            return toReturn;
        });
    }

    boolean areFlags(String[] arguments, String[] flags) {
        int aIndex = 0; // only search through arguments once
        for (String s : flags) {
            // for each flag
            String flag = s;
            if (flag != null && flag.length() > 0) {
                flag = flag.trim();
                // find it in arguments
                for (; aIndex < arguments.length; aIndex++) {
                    if (flag.equals(arguments[aIndex])) {
                        System.out.println("found flags");
                        return true;
                    }
                }
            }
        }
        System.out.println("no flags");
        return false;
    }

    /**
     * This will parse arguments into parts that had been separated by flags in
     * the form of Strings.
     *
     * @param arguments       to parse
     * @param flags           flags to separate the words
     * @param expectedNumArgs how many arguments are expected to come from this
     * @return As many strings as expected that had been separated by the flags
     */
    String[] prepositionSeparator(String[] arguments, String[] flags, int expectedNumArgs) {
        int argIndex = 0;
        String[] result = new String[expectedNumArgs];
        int fillIndex = 0;

        for (String preposition : flags) {
            // which preposition?
            if (preposition == null || preposition.length() == 0) {
                continue;
            }

            // while arg is not preposition
            result[fillIndex] = "";
            while (argIndex < arguments.length && !preposition.equals(arguments[argIndex])) {
                String arg = arguments[argIndex];
                if (arg != null && arg.length() > 0) {
                    // add it to that part of result
                    result[fillIndex] += arguments[argIndex] + ' ';
                }
                argIndex++;
            }
            result[fillIndex] = result[fillIndex].trim();
            fillIndex++;
            argIndex++;
        }

        // get the rest
        result[fillIndex] = "";
        for (; argIndex < arguments.length; argIndex++) {
            result[fillIndex] += arguments[argIndex] + ' ';
        }
        result[fillIndex] = result[fillIndex].trim();

        System.out.println("PrepositionSeparator:" + Arrays.toString(result));
        return result;

    }
}
