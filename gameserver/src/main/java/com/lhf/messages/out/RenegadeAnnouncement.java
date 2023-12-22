package com.lhf.messages.out;

import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class RenegadeAnnouncement extends OutMessage {
    private final ICreature turned;

    public static class Builder extends OutMessage.Builder<Builder> {
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
        public RenegadeAnnouncement Build() {
            return new RenegadeAnnouncement(this);
        }

    }

    public static Builder getBuilder(ICreature turned) {
        return new Builder(turned);
    }

    public RenegadeAnnouncement(Builder builder) {
        super(builder);
        this.turned = builder.getTurned();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append(this.addressCreature(turned, true))
                    .append(" has attacked a member or ally of their faction and thus became a RENEGADE. ")
                    .append("Until ").append(turned.getColorTaggedName())
                    .append(" rejoins a faction (certain spells can do this) consequences for attacking ")
                    .append(turned.getColorTaggedName()).append(" are removed.");
        } else {
            sb.append("You have attacked someone in your faction, or a faction ally, and have become a RENEGADE.")
                    .append("\n");
            sb.append(
                    "You may lose bonuses that you previously had, and consequences for attacking you are removed.")
                    .append("\n");
            sb.append(
                    "If you want to rejoin a faction, some Cube Holders have spells that can join you to a faction.");
        }
        return sb.toString();
    }

    public ICreature getTurned() {
        return this.turned;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
