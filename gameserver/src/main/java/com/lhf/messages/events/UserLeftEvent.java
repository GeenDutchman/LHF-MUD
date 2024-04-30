package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.messages.GameEventType;
import com.lhf.server.client.user.User;

public class UserLeftEvent extends GameEvent {
    private final User user;

    public static class Builder extends GameEvent.Builder<Builder> {
        private User user;

        protected Builder() {
            super(GameEventType.USER_LEFT);
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
        public UserLeftEvent Build() {
            return new UserLeftEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public UserLeftEvent(Builder builder) {
        super(builder);
        this.user = builder.getUser();
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            if (this.user != null) {
                builder.appendTaggable(user).appendString("has left the server\r\n");
                return;
            }
            builder.appendString("Goodbye, whoever it was that jsut left!");
            return;
        }
        builder.appendString("Goodbye, we hope to see you again soon!");
    }

    public User getUser() {
        return user;
    }

}
