package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.server.client.CommandInvoker;

public class SpeakingEvent extends GameEvent {
    private final String message;
    private final CommandInvoker sayer;
    private final CommandInvoker hearer;
    private final boolean shouting;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String message;
        private CommandInvoker sayer;
        private CommandInvoker hearer;
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

        public CommandInvoker getSayer() {
            return sayer;
        }

        public Builder setSayer(CommandInvoker sayer) {
            this.sayer = sayer;
            return this;
        }

        public CommandInvoker getHearer() {
            return hearer;
        }

        public Builder setHearer(CommandInvoker hearer) {
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.sayer != null) {
            builder.appendTaggable(sayer);
        } else {
            builder.appendString("Someone");
        }
        if (this.shouting) {
            builder.appendString("SHOUTS");
        }
        if (this.hearer != null) {
            builder.appendTaggable(hearer, " to ", null);
        }
        builder.appendString(message, ":", null);
    }

    public String getMessage() {
        return message;
    }

    public CommandInvoker getSayer() {
        return sayer;
    }

    public CommandInvoker getHearer() {
        return hearer;
    }

    public Boolean getShouting() {
        return shouting;
    }

}
