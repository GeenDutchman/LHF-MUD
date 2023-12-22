package com.lhf.messages.out;

import com.lhf.game.creature.ICreature;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.GameEventType;

public class FactionReinforcementsCallEvent extends GameEvent {
    private final ICreature caller;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature caller;

        protected Builder() {
            super(GameEventType.REINFORCEMENTS_CALL);
        }

        public ICreature getCaller() {
            return caller;
        }

        public Builder setCaller(ICreature caller) {
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
        public FactionReinforcementsCallEvent Build() {
            return new FactionReinforcementsCallEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public FactionReinforcementsCallEvent(Builder builder) {
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

    public ICreature getCaller() {
        return caller;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
