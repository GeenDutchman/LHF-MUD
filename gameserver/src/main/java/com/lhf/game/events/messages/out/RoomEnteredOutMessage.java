package com.lhf.game.events.messages.out;

import com.lhf.game.events.messages.ClientMessenger;
import com.lhf.game.events.messages.OutMessageType;

public class RoomEnteredOutMessage extends OutMessage {
    private final ClientMessenger newbie;

    public static class Builder extends OutMessage.Builder<Builder> {
        private ClientMessenger newbie;

        protected Builder() {
            super(OutMessageType.ROOM_ENTERED);
        }

        public ClientMessenger getNewbie() {
            return newbie;
        }

        public Builder setNewbie(ClientMessenger newbie) {
            this.newbie = newbie;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public RoomEnteredOutMessage Build() {
            return new RoomEnteredOutMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public RoomEnteredOutMessage(Builder builder) {
        super(builder);
        this.newbie = builder.getNewbie();
    }

    @Override
    public String toString() {
        return (this.newbie != null ? this.newbie.getColorTaggedName() : "Someone") + " has entered the room";
    }

    public ClientMessenger getNewbie() {
        return newbie;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
