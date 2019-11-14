package com.lhf.messages.out;

import com.lhf.user.User;

public class UserLeftMessage extends OutMessage {
    User user;
    public UserLeftMessage(User user) {
        this.user = user;
    }
    public String toString() {
        return user.getUsername() + " has left the server\n\r";
    }
}
