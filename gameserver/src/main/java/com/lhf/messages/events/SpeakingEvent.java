package com.lhf.messages.events;

import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.GameEventType;

public class SpeakingEvent extends GameEvent {
    private final String message;
    private final GameEventProcessor sayer;
    private final GameEventProcessor hearer;
    private final boolean shouting;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String message;
        private GameEventProcessor sayer;
        private GameEventProcessor hearer;
        private boolean shouting = false;

        protected Builder() {
            super(GameEventType.SPEAKING);
        }

        public String getMessage() {
            return message;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public GameEventProcessor getSayer() {
            return sayer;
        }

        public Builder setSayer(GameEventProcessor sayer) {
            this.sayer = sayer;
            return this;
        }

        public GameEventProcessor getHearer() {
            return hearer;
        }

        public Builder setHearer(GameEventProcessor hearer) {
            this.hearer = hearer;
            return this;
        }

        public boolean isShouting() {
            return shouting;
        }

        public Builder setShouting(boolean shouting) {
            this.shouting = shouting;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SpeakingEvent Build() {
            return new SpeakingEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpeakingEvent(Builder builder) {
        super(builder);
        this.sayer = builder.getSayer();
        this.message = builder.getMessage();
        this.shouting = builder.isShouting();
        this.hearer = builder.getHearer();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.sayer != null) {
            sb.append(this.sayer.getColorTaggedName());
        } else {
            sb.append("Someone");
        }
        if (this.shouting) {
            sb.append(" SHOUTS ");
        }
        if (this.hearer != null) {
            sb.append(" to ").append(this.hearer.getColorTaggedName());
        }
        sb.append(":").append(this.message);
        return sb.toString();
    }

    public String getMessage() {
        return message;
    }

    public GameEventProcessor getSayer() {
        return sayer;
    }

    public GameEventProcessor getHearer() {
        return hearer;
    }

    public Boolean getShouting() {
        return shouting;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
