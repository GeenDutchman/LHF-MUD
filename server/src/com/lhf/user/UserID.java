package com.lhf.user;

import com.lhf.messages.in.CreateInMessage;

public class UserID {
    private String username;
    public UserID(CreateInMessage create_user) {
        username = create_user.getUsername();
    }

    public UserID(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserID) {
            UserID userObj = (UserID) obj;
            if (userObj.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
