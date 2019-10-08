package com.lhf.messages;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMessage {
    public static UserMessage fromString(String payload) {
        String[] words = payload.split(" ");
        Stream<String> stream = Arrays.stream(words);
        Optional<String> first = Optional.ofNullable(words[0]);
        String arguments = stream.skip(1).collect(Collectors.joining(" "));
        return first.map(val -> {
            switch (val) {
                case "say":
                    return new SayMessage(arguments);
                case "exit":
                    return new ExitMessage();
                default:
                    return new BadMessage();
            }
        }).orElse(new BadMessage());
    }
}
