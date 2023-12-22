package com.lhf.messages.out;

import com.lhf.messages.GameEventType;

public class NoUserMessage extends GameEvent {
    public static class Builder extends GameEvent.Builder<Builder> {
        protected Builder() {
            super(GameEventType.NO_USER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public NoUserMessage Build() {
            return new NoUserMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public NoUserMessage(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "You have neither created nor logged in as a User, so you can't do much yet.\r\nTry running 'CREATE <username> with <password>'\r\n";
    }

    @Override
    public String print() {
        return this.toString();
    }
}
