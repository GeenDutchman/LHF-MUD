package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;
import com.lhf.server.client.user.UserID;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TellMessage extends InMessage {
    private String message;
    private UserID target;

    TellMessage(String payload) {
        String[] words = payload.split(" ");
        Stream<String> stream = Arrays.stream(words);
        String username = words[0]; // This will just crash for now. Need more fault tolerance for malformed
                                    // messages
        this.message = stream.skip(1).collect(Collectors.joining(" "));
        target = new UserID(username);
    }

    public UserID getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.TELL;
    }
}
