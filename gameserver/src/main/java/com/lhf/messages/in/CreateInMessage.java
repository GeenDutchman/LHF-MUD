package com.lhf.messages.in;

import com.lhf.messages.CommandMessage;

public class CreateInMessage extends InMessage {
    private String username;
    private String password;

    CreateInMessage(String payload) {
        if (payload.length() > 1 && payload.contains(" ")) {
            String[] words = payload.split(" ");
            if (words.length != 2) { // We only want this command to have 2 args
                username = "";
                password = "";
            } else { // If the command has 2 args, it should work properly
                username = words[0];
                password = words[1];
            }
        } else { // Command must have 2 args; if not username and password are both empty string
            username = "";
            password = "";
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public CommandMessage getType() {
        return CommandMessage.CREATE;
    }
}
