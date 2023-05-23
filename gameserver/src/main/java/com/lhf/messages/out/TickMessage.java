package com.lhf.messages.out;

import com.lhf.game.TickType;
import com.lhf.messages.ITickMessage;
import com.lhf.messages.OutMessageType;

public class TickMessage extends OutMessage implements ITickMessage {
    private final TickType tickType;

    public static class Builder extends OutMessage.Builder<Builder> {
        private TickType tickType;

        protected Builder() {
            super(OutMessageType.TICK);
            this.tickType = TickType.INSTANT;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public TickMessage Build() {
            return new TickMessage(this);
        }

        public TickType getTickType() {
            return tickType;
        }

        public Builder setTickType(TickType tickType) {
            this.tickType = tickType != null ? tickType : TickType.INSTANT;
            return this;
        }

        public Builder(OutMessageType type) {
            super(type);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public TickMessage(Builder builder) {
        super(builder);
        this.tickType = builder.getTickType();
    }

    @Override
    public TickType getTickType() {
        return this.tickType;
    }

    @Override
    public String print() {
        return "";
    }

}
