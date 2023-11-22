package com.lhf.server.client.user;

import com.lhf.messages.in.CreateInMessage;

public class UserID {
    private final String username;

    public UserID(final CreateInMessage create_user) {
        username = create_user.getUsername();
    }

    public UserID(final String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
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
    public boolean equals(final Object obj) {
        if (obj instanceof UserID) {
            final UserID userObj = (UserID) obj;
            return userObj.getUsername().equals(username);
        }
        return false;
    }
}
