package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class DuplicateUserMessage extends WelcomeMessage {

    public static class Builder extends WelcomeMessage.AbstractBuilder<Builder> {

        protected Builder() {
            super(OutMessageType.DUPLICATE_USER);
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public DuplicateUserMessage Build() {
            return new DuplicateUserMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public DuplicateUserMessage(Builder builder) {
        super(builder);
    }

    public String toString() {
        return super.toString()
                + "\r\nAn adventurer by that name already exists! Please name your adventurer something unique.";
    }

    @Override
    public String print() {
        return this.toString();
    }
}
