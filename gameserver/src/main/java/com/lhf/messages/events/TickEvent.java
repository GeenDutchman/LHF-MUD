package com.lhf.messages.events;

import java.util.Optional;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;
import com.lhf.messages.ITickEvent;

public class TickEvent extends GameEvent implements ITickEvent {
    private final TickType tickType;
    private final Optional<String> tickSpecificity;

    public static class Builder extends GameEvent.Builder<Builder> {
        private TickType tickType;
        private String tickSpecificity;

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

        public Builder fromITickEvent(ITickEvent event) {
            if (event != null) {
                this.tickType = event.getTickType() != null ? event.getTickType() : TickType.INSTANT;
                this.tickSpecificity = event.getTickSpecificity().orElse(null);
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

        public String getTickSpecificity() {
            return tickSpecificity;
        }

        public Builder setTickSpecificity(String tickSpecificity) {
            this.tickSpecificity = tickSpecificity;
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
        this.tickSpecificity = Optional.ofNullable(builder.getTickSpecificity());
    }

    @Override
    public Optional<String> getTickSpecificity() {
        return this.tickSpecificity;
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
