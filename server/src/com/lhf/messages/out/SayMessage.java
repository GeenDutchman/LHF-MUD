package com.lhf.messages.out;

import com.lhf.user.User;

public class SayMessage extends OutMessage {
    private String message;
    private User user;
    public SayMessage(String payload, User user) {
        message = payload;
        this.user = user;
    }
    @Override
    public String toString() {
        return user.getUsername() + ": " + message + "\n\r";
    }
}
