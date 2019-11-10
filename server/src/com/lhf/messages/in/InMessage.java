package com.lhf.messages.in;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMessage {
    public static Optional<InMessage> fromString(String payload) {
        String[] words = payload.split(" ");
        Stream<String> stream = Arrays.stream(words);
        Optional<String> first = Optional.ofNullable(words[0]);
        String arguments = stream.skip(1).collect(Collectors.joining(" "));
        return first.flatMap(val -> {
            switch (val.toLowerCase()) {
                case "say":
                    return Optional.of(new SayMessage(arguments));
                case "tell":
                    return Optional.of(new TellMessage(arguments));
                case "exit":
                    return Optional.of(new ExitMessage());
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
                case "look":
                    return Optional.of(new LookMessage());
                case "take":
                    return Optional.of(new TakeMessage(arguments));
                case "drop":
                    return Optional.of(new DropMessage(arguments));
                case "inventory":
                    return Optional.of(new InventoryMessage());
                case "equip":
                    return Optional.of(new EquipMessage(arguments));
                case "unequip":
                    return Optional.of(new UnequipMessage(arguments));
                default:
                    return Optional.empty();
            }
        });
    }
}
