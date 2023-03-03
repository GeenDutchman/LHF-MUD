package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.OutMessageType;

public class FleeMessage extends OutMessage {
    private final Creature runner;
    private final MultiRollResult roll;
    private final boolean fled;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature runner;
        private MultiRollResult roll;
        private boolean fled;

        protected Builder() {
            super(OutMessageType.FLEE);
        }

        public Creature getRunner() {
            return runner;
        }

        public Builder setRunner(Creature runner) {
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
        public OutMessage Build() {
            return new FleeMessage(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public FleeMessage(Builder builder) {
        super(builder);
        this.runner = builder.getRunner();
        this.roll = builder.getRoll();
        this.fled = builder.isFled();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.addressCreature(this.runner));
        if (this.fled) {
            sb.append(" successfully fled from the battle");
        } else {
            sb.append(" attempted fleeing from the battle, but failed");
        }
        if (!this.isBroadcast() && this.roll != null) {
            sb.append(" ").append(this.roll.getColorTaggedName());
        }
        sb.append("!");

        return sb.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

    public Creature getRunner() {
        return runner;
    }

    public MultiRollResult getRoll() {
        return roll;
    }

    public boolean isFled() {
        return fled;
    }

}
