package com.lhf.messages.out;

import com.lhf.game.creature.Creature;
import com.lhf.messages.OutMessageType;

public class RenegadeAnnouncement extends OutMessage {
    private final Creature turned;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature turned;

        protected Builder(Creature turned) {
            super(OutMessageType.RENEGADE_ANNOUNCEMENT);
            this.turned = turned;
        }

        public Creature getTurned() {
            return turned;
        }

        public Builder setTurned(Creature turned) {
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

    public RenegadeAnnouncement(Builder builder) {
        super(builder);
        this.turned = builder.getTurned();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append(this.addressCreature(turned, true))
                    .append(" has attacked a member of their faction and thus became a RENEGADE. ")
                    .append("Until ").append(turned.getColorTaggedName())
                    .append(" rejoins a faction (certain spells can do this) consequences for attacking ")
                    .append(turned.getColorTaggedName()).append(" are removed.");
        } else {
            sb.append("You have attacked someone in your faction, and have become a RENEGADE.").append("\n");
            sb.append(
                    "You may lose bonuses that you previously had, and consequences for attacking you are removed.")
                    .append("\n");
            sb.append(
                    "If you want to rejoin a faction, some casters have spells that can join you to a faction.");
        }
        return sb.toString();
    }

    public Creature getTurned() {
        return this.turned;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
