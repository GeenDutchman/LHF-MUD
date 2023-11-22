package com.lhf.game.events.messages.out;

import com.lhf.game.events.messages.OutMessageType;
import com.lhf.server.client.user.User;

public class UserLeftMessage extends OutMessage {
    private final User user;

    public static class Builder extends OutMessage.Builder<Builder> {
        private User user;

        protected Builder() {
            super(OutMessageType.USER_LEFT);
        }

        public User getUser() {
            return user;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public UserLeftMessage Build() {
            return new UserLeftMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public UserLeftMessage(Builder builder) {
        super(builder);
        this.user = builder.getUser();
    }

    public String toString() {
        if (this.isBroadcast()) {
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

    @Override
    public String print() {
        return this.toString();
    }

}
