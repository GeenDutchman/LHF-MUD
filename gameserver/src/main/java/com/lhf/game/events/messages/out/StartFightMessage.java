package com.lhf.game.events.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.events.messages.OutMessageType;

public class StartFightMessage extends OutMessage {
    private final Creature instigator;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature instigator;

        protected Builder() {
            super(OutMessageType.START_FIGHT);
        }

        public Creature getInstigator() {
            return instigator;
        }

        public Builder setInstigator(Creature instigator) {
            this.instigator = instigator;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public OutMessage Build() {
            return new StartFightMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public StartFightMessage(Builder builder) {
        super(builder);
        this.instigator = builder.getInstigator();
    }

    @Override
    public String toString() {
        if (!this.isBroadcast()) {
            return "You are in the fight!";
        }
        return this.instigator.getColorTaggedName() + " started a fight!";
    }

    public Creature getInstigator() {
        return instigator;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
