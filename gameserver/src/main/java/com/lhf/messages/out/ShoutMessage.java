package com.lhf.messages.out;

import com.lhf.server.client.user.User;

public class ShoutMessage extends OutMessage {
    private String message;
    private User user;

    public ShoutMessage(String payload, User user) {
        message = payload;
        this.user = user;
    }

    @Override
    public String toString() {
        return user.getUsername() + ": " + message + "\r\n";
    }
}