package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class UserCreatedEvent extends GameEvent {
    public static class Builder extends GameEvent.Builder<Builder> {
        protected Builder() {
            super(GameEventType.USER_CREATED);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public UserCreatedEvent Build() {
            return new UserCreatedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public UserCreatedEvent(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "New User in Server\r\n";
    }

    @Override
    public String print() {
        return this.toString();
    }
}
