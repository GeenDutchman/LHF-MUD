package com.lhf.messages.out;

import com.lhf.server.client.user.User;

public class UserLeftMessage extends OutMessage {
    private User user;

    public UserLeftMessage(User user) {
        this.user = user;
    }

    public String toString() {
        return user.getUsername() + " has left the server\r\n";
    }
}
