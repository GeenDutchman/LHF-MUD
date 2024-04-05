package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class BattleStartedEvent extends GameEvent {
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
            return new BattleStartedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleStartedEvent(Builder builder) {
        super(builder);
        this.instigator = builder.getInstigator();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public ICreature getInstigator() {
        return instigator;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            builder.appendTaggable(instigator);
            builder.appendString("started a fight!");
        } else {
            builder.appendString("You are in the fight!");
        }
    }

}
