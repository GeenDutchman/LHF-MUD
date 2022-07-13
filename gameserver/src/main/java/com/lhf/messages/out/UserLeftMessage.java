package com.lhf.messages.out;

import com.lhf.server.client.user.User;

public class UserLeftMessage extends OutMessage {
    private User user;
    private boolean addressUser;

    public UserLeftMessage(User user, boolean addressUser) {
        this.user = user;
        this.addressUser = addressUser;
    }

    public String toString() {
        if (!this.addressUser) {
            return user.getUsername() + " has left the server\r\n";
        }
        return "Goodbye, we hope to see you again soon!";
    }

    public User getUser() {
        return user;
    }

    public boolean isAddressUser() {
        return addressUser;
    }
}
