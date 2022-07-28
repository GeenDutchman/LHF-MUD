package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;
import com.lhf.server.client.user.User;

public class UserLeftMessage extends OutMessage {
    private User user;
    private boolean addressUser;

    public UserLeftMessage(User user, boolean addressUser) {
        super(OutMessageType.USER_LEFT);
        this.user = user;
        this.addressUser = addressUser;
    }

    public String toString() {
        if (!this.addressUser) {
            if (this.user != null) {
                return user.getUsername() + " has left the server\r\n";
            }
            return "Goodbye, whoever it was that just left!";
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
