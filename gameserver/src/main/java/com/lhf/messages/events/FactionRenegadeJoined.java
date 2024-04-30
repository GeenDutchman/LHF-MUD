package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class FactionRenegadeJoined extends GameEvent {
    private final ICreature turned;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature turned;

        protected Builder(ICreature turned) {
            super(GameEventType.RENEGADE_ANNOUNCEMENT);
            this.turned = turned;
        }

        public ICreature getTurned() {
            return turned;
        }

        public Builder setTurned(ICreature turned) {
            this.turned = turned;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public FactionRenegadeJoined Build() {
            return new FactionRenegadeJoined(this);
        }

    }

    public static Builder getBuilder(ICreature turned) {
        return new Builder(turned);
    }

    public FactionRenegadeJoined(Builder builder) {
        super(builder);
        this.turned = builder.getTurned();
    }

    public ICreature getTurned() {
        return this.turned;
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            this.addressCreature(builder, turned);
            builder.appendString("has attacked a member or ally of their faciton and thus became a RENEGADE. Until");
            builder.appendTaggable(turned);
            builder.appendString(
                    "rejoins a faciton (certain spells can do this) consequences for attacking them are removed.");
        } else {
            builder.appendString(
                    "You have attacked someone in your faction, or a faction ally, and have become a RENEGADE.");
            builder.appendString(
                    "You may lose bonuses that you previously had, and consequences for attacking you are removed.");
            builder.appendString(
                    "If you want to rejoin a faction, some Cube Holders have spells that can join you to a faction.");
        }
    }

}
