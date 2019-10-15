package com.lhf.messages.in;

public class CreateInMessage extends InMessage {
    String username;
    String password;
    public CreateInMessage(String payload) {
         String[] words = payload.split(" ");
         username = words[0];
         password = words[1];
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
