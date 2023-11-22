package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.OutMessageType;

public class ReinforcementsCall extends OutMessage {
    private final Creature caller;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature caller;

        protected Builder() {
            super(OutMessageType.REINFORCEMENTS_CALL);
        }

        public Creature getCaller() {
            return caller;
        }

        public Builder setCaller(Creature caller) {
            this.caller = caller;
            return this;
        }

        public Builder setCallerAddressed(boolean callerAddressed) {
            if (callerAddressed) {
                this.setNotBroadcast();
            } else {
                this.setBroacast();
            }
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ReinforcementsCall Build() {
            return new ReinforcementsCall(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ReinforcementsCall(Builder builder) {
        super(builder);
        this.caller = builder.getCaller();
    }

    @Override
    public String toString() {
        if (!this.isBroadcast()) {
            if (this.caller.getFaction() == null || CreatureFaction.RENEGADE.equals(this.caller.getFaction())) {
                return "You are a RENEGADE or not a member of a faction.  No one is obligated to help you.";
            }
            return "You call for reinforcements!";
        } else {
            return this.caller.getColorTaggedName() + " calls for reinforcements!";
        }
    }

    public Creature getCaller() {
        return caller;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
