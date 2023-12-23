package com.lhf.messages.events;

import com.lhf.messages.GameEventType;
import com.lhf.server.client.CommandInvoker;

public class RoomEnteredEvent extends GameEvent {
    private final CommandInvoker newbie;

    public static class Builder extends GameEvent.Builder<Builder> {
        private CommandInvoker newbie;

        protected Builder() {
            super(GameEventType.ROOM_ENTERED);
        }

        public CommandInvoker getNewbie() {
            return newbie;
        }

        public Builder setNewbie(CommandInvoker newbie) {
            this.newbie = newbie;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public RoomEnteredEvent Build() {
            return new RoomEnteredEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public RoomEnteredEvent(Builder builder) {
        super(builder);
        this.newbie = builder.getNewbie();
    }

    @Override
    public String toString() {
        return (this.newbie != null ? this.newbie.getColorTaggedName() : "Someone") + " has entered the room";
    }

    public CommandInvoker getNewbie() {
        return newbie;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
