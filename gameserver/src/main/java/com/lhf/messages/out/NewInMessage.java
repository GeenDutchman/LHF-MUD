package com.lhf.messages.out;

import com.lhf.messages.OutMessageType;

public class NewInMessage extends OutMessage {
    public static class Builder extends OutMessage.Builder<Builder> {
        protected Builder() {
            super(OutMessageType.NEW_IN);
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
