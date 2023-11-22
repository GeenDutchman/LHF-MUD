package com.lhf.game.events.messages.out;

import java.util.Objects;
import java.util.UUID;

import com.lhf.game.creature.Creature;
import com.lhf.game.events.messages.OutMessageType;

public abstract class OutMessage implements Comparable<OutMessage> {

    public static abstract class Builder<T extends Builder<T>> {
        private OutMessageType type;
        private boolean broadcast;
        protected T thisObject;

        protected Builder(OutMessageType type) {
            this.type = type;
            this.broadcast = false;
            this.thisObject = this.getThis();
        }

        // public T setType(OutMessageType type) {
        // this.type = type;
        // return this.getThis();
        // }

        public OutMessageType getType() {
            return this.type;
        }

        public T setBroacast() {
            this.broadcast = true;
            return this.getThis();
        }

        public T setNotBroadcast() {
            this.broadcast = false;
            return this.getThis();
        }

        public boolean isBroadcast() {
            return this.broadcast;
        }

        public abstract T getThis();

        public abstract OutMessage Build();
    }

    private final OutMessageType type;
    private final boolean broadcast;
    private final Builder<?> builder;
    private final UUID uuid;

    public OutMessage(Builder<?> builder) {
        this.type = builder.getType();
        this.broadcast = builder.isBroadcast();
        this.uuid = UUID.randomUUID();
        this.builder = builder;
    }

    public Builder<?> copyBuilder() {
        return this.builder;
    }

    public OutMessageType getOutType() {
        return this.type;
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }

    protected String addressCreature(Creature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            return capitalize ? "You" : "you";
        } else if (creature != null) {
            return creature.getColorTaggedName();
        } else {
            return capitalize ? "Someone" : "someone";
        }
    }

    protected String possesiveCreature(Creature creature, boolean capitalize) {
        if (!this.isBroadcast()) {
            return capitalize ? "Your" : "your";
        } else if (creature != null) {
            return creature.getColorTaggedName() + "'s";
        } else {
            return capitalize ? "Their" : "their";
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    // Called to render as a human-readable string
    public abstract String print();

    @Override
    public int hashCode() {
        return Objects.hash(type, uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OutMessage)) {
            return false;
        }
        OutMessage other = (OutMessage) obj;
        return type == other.type && Objects.equals(uuid, other.uuid);
    }

    @Override
    public int compareTo(OutMessage arg0) {
        int runningCompare = this.type.compareTo(arg0.getOutType());
        if (runningCompare != 0) {
            return runningCompare;
        }
        return this.uuid.compareTo(arg0.uuid);
    }

}
