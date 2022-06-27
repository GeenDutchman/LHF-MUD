package com.lhf.messages.out;

import com.lhf.game.creature.Creature;

public class RenegadeAnnouncement extends OutMessage {
    private Creature turned;

    public RenegadeAnnouncement() {
        this.turned = null;
    }

    public RenegadeAnnouncement(Creature turned) {
        this.turned = turned;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.turned != null) {
            sb.append(turned.getColorTaggedName())
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
}
