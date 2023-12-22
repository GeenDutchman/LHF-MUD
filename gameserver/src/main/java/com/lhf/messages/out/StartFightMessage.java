package com.lhf.messages.out;

import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class StartFightMessage extends GameEvent {
    private final ICreature instigator;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature instigator;

        protected Builder() {
            super(GameEventType.START_FIGHT);
        }

        public ICreature getInstigator() {
            return instigator;
        }

        public Builder setInstigator(ICreature instigator) {
            this.instigator = instigator;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public GameEvent Build() {
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

    public ICreature getInstigator() {
        return instigator;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
