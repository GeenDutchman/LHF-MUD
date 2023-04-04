package com.lhf.server.client.user;

import com.lhf.messages.in.CreateInMessage;

public class UserID {
    private String username;

    public UserID(CreateInMessage create_user) {
        username = create_user.getUsername();
    }

    public UserID(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserID [username=").append(username).append("]");
        return builder.toString();
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
            return userObj.getUsername().equals(username);
        }
        return false;
    }
}
