package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.messages.GameEventType;

public class BattleRoundWastedEvent extends BattleRoundEvent {
    private final int wastedPenalty;

    public static class Builder extends BattleRoundEvent.Builder {
        private int wastedPenalty;

        protected Builder() {
            super(GameEventType.BATTLE_TURN_WASTED);
            super.setNeedSubmission(RoundAcceptance.MISSING);
        }

        @Override
        public Builder setNeedSubmission(RoundAcceptance isSubmissionNeeded) {
            if (isSubmissionNeeded != RoundAcceptance.MISSING) {
                throw new IllegalArgumentException(
                        "Cannot set RoundAcceptance to anything other than Missing for this type");
            }
            super.setNeedSubmission(RoundAcceptance.MISSING);
            return this;
        }

        // Will coerce it to a value <= 0
        public Builder setWastedPenalty(int penalty) {
            this.wastedPenalty = penalty <= 0 ? penalty : -1 * penalty;
            return this;
        }

        public int getWastedPenalty() {
            return wastedPenalty;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BattleRoundWastedEvent Build() {
            return new BattleRoundWastedEvent(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleRoundWastedEvent(Builder builder) {
        super(builder);
        this.wastedPenalty = builder.getWastedPenalty();
    }

    public int getWastedPenalty() {
        return wastedPenalty;
    }

    @Override
    public String print() {
        return this.toString();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ").setEmptyValue("This is a turn notification");
        sj.add(super.toString());

        if (this.wastedPenalty != 0) {
            sj.add(this.addressCreature(this.about, true)).add("incurred a penalty of")
                    .add(Integer.toString(this.wastedPenalty)).add("damage");
        }
        return sj.toString() + "!";
    }

}
