package com.lhf.messages.in;

public class CreateInMessage extends InMessage {
    String username;
    String password;
    public CreateInMessage(String payload) {
        if (payload.length() > 1 && payload.contains(" ")) {
            String[] words = payload.split(" ");
            username = words[0];
            password = words[1];
        } else {
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
}
