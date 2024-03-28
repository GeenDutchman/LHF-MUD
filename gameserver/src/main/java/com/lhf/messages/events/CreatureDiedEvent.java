package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class CreatureDiedEvent extends GameEvent {
    private final ICreature dearlyDeparted;
    private final Taggable cause;
    private final String extraInfo;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature dearlyDeparted;
        private Taggable cause;
        private String extraInfo;

        protected Builder() {
            super(GameEventType.CREATURE_DIED);
        }

        public ICreature getDearlyDeparted() {
            return dearlyDeparted;
        }

        public Builder setDearlyDeparted(ICreature dearlyDeparted) {
            if (dearlyDeparted == null || dearlyDeparted.isAlive()) {
                throw new IllegalArgumentException(String.format("Cannot announce that %s is dead!", dearlyDeparted));
            }
            this.dearlyDeparted = dearlyDeparted;
            return this;
        }

        public Taggable getCause() {
            return cause;
        }

        public Builder setCause(Taggable cause) {
            this.cause = cause;
            return this;
        }

        public String getExtraInfo() {
            return extraInfo;
        }

        public Builder setExtraInfo(String extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureDiedEvent Build() {
            return new CreatureDiedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureDiedEvent(Builder builder) {
        super(builder);
        this.dearlyDeparted = builder.getDearlyDeparted();
        this.cause = builder.getCause();
        this.extraInfo = builder.getExtraInfo();
    }

    public ICreature getDearlyDeparted() {
        return dearlyDeparted;
    }

    public Taggable getCause() {
        return cause;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public TickType getTickType() {
        return TickType.DEATH;
    }

    @Override
    public String printString() {
        ICreature dead = this.getDearlyDeparted();
        if (dead == null || dead.isAlive()) {
            return "JK!  Nobody died!";
        }
        StringJoiner sj = new StringJoiner(" ");
        sj.add(dead.getName()).add("has died.");
        Taggable cause = this.getCause();
        if (cause != null) {
            sj.add("They died because of:").add(cause.getSimpleContent() + ".");
        }
        String extras = this.getExtraInfo();
        if (extras != null && !extras.isBlank()) {
            sj.add(extras);
        }
        return sj.toString();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        ICreature dead = this.getDearlyDeparted();

        if (dead == null || dead.isAlive()) {
            myElement.appendChild(nodeGenerator.createTextNode("JK! Nobody died!"));
            return myElement;
        }
        myElement.appendChild(dead.buildXMLElement(nodeGenerator));
        myElement.appendChild(nodeGenerator.createTextNode(" has died."));
        Taggable cause = this.getCause();
        if (cause != null) {
            myElement.appendChild(nodeGenerator.createTextNode(" They died because of: "));
            myElement.appendChild(cause.buildXMLElement(nodeGenerator));
            myElement.appendChild(nodeGenerator.createTextNode("."));
        }
        String extras = this.getExtraInfo();
        if (extras != null && !extras.isBlank()) {
            Element extraElement = nodeGenerator.createElement("Extras");
            extraElement.appendChild(nodeGenerator.createTextNode(extras));
            myElement.appendChild(extraElement);
        }
        return myElement;
    }
}
