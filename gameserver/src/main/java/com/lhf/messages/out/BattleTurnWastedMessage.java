package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.messages.OutMessageType;

public class BattleTurnWastedMessage extends OutMessage {
    private final Creature waster;
    private final int wastedPenalty;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature waster;
        private int wastedPenalty;

        protected Builder() {
            super(OutMessageType.BATTLE_TURN_WASTED);
        }

        public Builder setWaster(Creature waster) {
            this.waster = waster;
            return this;
        }

        public Creature getWaster() {
            return this.waster;
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
        public BattleTurnWastedMessage Build() {
            return new BattleTurnWastedMessage(this);
        }
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BattleTurnWastedMessage(Builder builder) {
        super(builder);
        this.wastedPenalty = builder.getWastedPenalty();
        this.waster = builder.getWaster();
    }

    public Creature getWaster() {
        return waster;
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
        if (this.isBroadcast()) {
            if (this.waster != null) {
                sj.add(this.waster.getColorTaggedName());
            } else {
                sj.add("Someone here");
            }
        } else {
            sj.add("You");
        }
        sj.add("wasted a turn");
        if (this.wastedPenalty != 0) {
            sj.add("and incurred a penalty of").add(Integer.toString(this.wastedPenalty)).add("damage");
        }
        return sj.toString() + "!";
    }

}
