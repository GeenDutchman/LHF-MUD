package com.lhf.messages.events;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class TickEvent extends GameEvent {
    private final TickType tickType;

    public static class Builder extends GameEvent.Builder<Builder> {
        private TickType tickType;

        protected Builder() {
            super(GameEventType.TICK);
            this.tickType = TickType.INSTANT;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public TickEvent Build() {
            return new TickEvent(this);
        }

        public Builder fromGameEvent(GameEvent event) {
            if (event != null) {
                this.tickType = event.getTickType() != null ? event.getTickType() : TickType.INSTANT;
            }
            return this;
        }

        public TickType getTickType() {
            return tickType;
        }

        public Builder setTickType(TickType tickType) {
            this.tickType = tickType != null ? tickType : TickType.INSTANT;
            return this;
        }

        public Builder(GameEventType type) {
            super(type);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public TickEvent(Builder builder) {
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
