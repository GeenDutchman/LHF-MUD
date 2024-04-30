package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
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

    public ICreature getCaller() {
        return caller;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            builder.appendTaggable(this.caller);
            builder.appendString("calls for reinforcements!");
        } else {
            if (this.caller != null && (this.caller.getFaction() == null
                    || CreatureFaction.RENEGADE.equals(this.caller.getFaction()))) {
                builder.appendString(
                        "You are a RENEGADE or not a member of a faction.  No one is obligated to help you.");
                return;
            }
            builder.appendString("You call for reinvorcements!");
        }
    }

}
