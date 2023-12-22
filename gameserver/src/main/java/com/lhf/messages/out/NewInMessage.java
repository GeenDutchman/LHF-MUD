package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class NewInMessage extends GameEvent {
    public static class Builder extends GameEvent.Builder<Builder> {
        protected Builder() {
            super(GameEventType.NEW_IN);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public NewInMessage Build() {
            return new NewInMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public NewInMessage(Builder builder) {
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
