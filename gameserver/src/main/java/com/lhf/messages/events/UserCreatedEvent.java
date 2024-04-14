package com.lhf.messages.events;

import com.lhf.OutputBuilder;
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.appendString("New user in Server\r\n");
    }

}
