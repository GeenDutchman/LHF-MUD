package com.lhf.server.messages.out;

import com.lhf.server.client.user.User;

public class SayMessage extends OutMessage {
    private String message;
    private User user;
    public SayMessage(String payload, User user) {
        message = payload;
        this.user = user;
    }
    @Override
    public String toString() {
        return user.getColorTaggedUsername() + ": " + message;
    }
}
