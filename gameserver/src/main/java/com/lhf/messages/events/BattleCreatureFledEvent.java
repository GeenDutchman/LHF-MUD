package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;

public class BattleCreatureFledEvent extends GameEvent {
    private final ICreature runner;
    private final MultiRollResult roll;
    private final boolean fled;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature runner;
        private MultiRollResult roll;
        private boolean fled;

        protected Builder() {
            super(GameEventType.FLEE);
        }

        public ICreature getRunner() {
            return runner;
        }

        public Builder setRunner(ICreature runner) {
            this.runner = runner;
            return this;
        }

        public MultiRollResult getRoll() {
            return roll;
        }

        public Builder setRoll(MultiRollResult roll) {
            this.roll = roll;
            return this;
        }

        public boolean isFled() {
            return fled;
        }

        public Builder setFled(boolean fled) {
            this.fled = fled;
            return this;
        }

        @Override
        public BattleCreatureFledEvent Build() {
            return new BattleCreatureFledEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleCreatureFledEvent(Builder builder) {
        super(builder);
        this.runner = builder.getRunner();
        this.roll = builder.getRoll();
        this.fled = builder.isFled();
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        this.addressCreature(builder, runner, true);
        if (this.fled) {
            builder.appendString("successfully fled from the battle");
        } else {
            builder.appendString("attempted fleeing from the battle, but failed");
        }
        if (!this.isBroadcast() && this.roll != null) {
            builder.appendTaggable(this.roll);
        }
        builder.appendString("!", null, null);
    }

    public ICreature getRunner() {
        return runner;
    }

    public MultiRollResult getRoll() {
        return roll;
    }

    public boolean isFled() {
        return fled;
    }

}
