package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    @Override
    public String toString() {
        return this.printString();
    }

    public ICreature getTurned() {
        return this.turned;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.isBroadcast()) {
            myElement.appendChild(this.addressCreatureXML(nodeGenerator, turned, true));
            myElement.appendChild(nodeGenerator.createTextNode(
                    " has attacked a member or ally of their faction and thus became a RENEGADE. Until "));
            myElement.appendChild(turned.buildXMLElement(nodeGenerator));
            myElement.appendChild(nodeGenerator.createTextNode(
                    " rejoins a faction (certain spells can do this) consequences for attacking them are removed."));
        } else {

            myElement.appendChild(nodeGenerator.createTextNode(
                    "You have attacked someone in your faction, or a faction ally, and have become a RENEGADE. "));
            myElement.appendChild(nodeGenerator.createTextNode(
                    "You may lose bonuses that you previously had, and consequences for attacking you are removed. "));
            myElement.appendChild(nodeGenerator.createTextNode(
                    "If you want to rejoin a faction, some Cube Holders have spells that can join you to a faction."));
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBroadcast()) {
            sb.append(this.addressCreature(turned, true))
                    .append(" has attacked a member or ally of their faction and thus became a RENEGADE. ")
                    .append("Until ").append(turned.getName())
                    .append(" rejoins a faction (certain spells can do this) consequences for attacking ")
                    .append(turned.getName()).append(" are removed.");
        } else {
            sb.append("You have attacked someone in your faction, or a faction ally, and have become a RENEGADE.")
                    .append("\n");
            sb.append("You may lose bonuses that you previously had, and consequences for attacking you are removed.")
                    .append("\n");
            sb.append("If you want to rejoin a faction, some Cube Holders have spells that can join you to a faction.");
        }
        return sb.toString();
    }

}
