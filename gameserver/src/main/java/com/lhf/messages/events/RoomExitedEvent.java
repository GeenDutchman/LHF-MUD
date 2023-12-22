package com.lhf.messages.events;

import com.lhf.game.map.Directions;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.GameEventType;

public class RoomExitedEvent extends GameEvent {
    private final ClientMessenger leaveTaker;
    private final Directions whichWay;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ClientMessenger leaveTaker;
        private Directions whichWay;

        protected Builder() {
            super(GameEventType.ROOM_EXITED);
        }

        public ClientMessenger getLeaveTaker() {
            return leaveTaker;
        }

        public Builder setLeaveTaker(ClientMessenger leaveTaker) {
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

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public RoomExitedEvent Build() {
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
        sb.append(".");
        return sb.toString();
    }

    public ClientMessenger getLeaveTaker() {
        return leaveTaker;
    }

    public Directions getWhichWay() {
        return whichWay;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
