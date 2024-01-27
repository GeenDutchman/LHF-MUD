package com.lhf.messages.events;

import com.lhf.Taggable;
import com.lhf.game.map.Directions;
import com.lhf.messages.GameEventType;
import com.lhf.server.client.CommandInvoker;

public class RoomExitedEvent extends GameEvent {
    private final CommandInvoker leaveTaker;
    private final Directions whichWay;
    private final Taggable becauseOf;

    public static class Builder extends GameEvent.Builder<Builder> {
        private CommandInvoker leaveTaker;
        private Directions whichWay;
        private Taggable becauseOf;

        protected Builder() {
            super(GameEventType.ROOM_EXITED);
        }

        public CommandInvoker getLeaveTaker() {
            return leaveTaker;
        }

        public Builder setLeaveTaker(CommandInvoker leaveTaker) {
            this.leaveTaker = leaveTaker;
            return this;
        }

        public Directions getWhichWay() {
            return whichWay;
        }

        public Builder setWhichWay(Directions whichWay) {
            this.whichWay = whichWay;
            return this;
        }

        public Taggable getBecauseOf() {
            return becauseOf;
        }

        public Builder setBecauseOf(Taggable becauseOf) {
            this.becauseOf = becauseOf;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public RoomExitedEvent Build() {
            if (this.becauseOf != null && this.whichWay != null) {
                throw new IllegalStateException(String.format("Cannot have both direction %s and becauseOf %s!",
                        this.whichWay, this.becauseOf));
            }
            return new RoomExitedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public RoomExitedEvent(Builder builder) {
        super(builder);
        this.leaveTaker = builder.getLeaveTaker();
        this.whichWay = builder.getWhichWay();
        this.becauseOf = builder.getBecauseOf();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.leaveTaker == null) {
            sb.append("Someone");
        } else {
            sb.append(leaveTaker.getColorTaggedName());
        }
        sb.append(" left the room");
        if (this.whichWay != null) {
            sb.append(" going ").append(this.whichWay.getColorTaggedName());
        }
        if (this.becauseOf != null) {
            sb.append(" because of ").append(this.becauseOf.getColorTaggedName());
        }
        sb.append(".");
        return sb.toString();
    }

    public CommandInvoker getLeaveTaker() {
        return leaveTaker;
    }

    public Directions getWhichWay() {
        return whichWay;
    }

    public Taggable getBecauseOf() {
        return becauseOf;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
