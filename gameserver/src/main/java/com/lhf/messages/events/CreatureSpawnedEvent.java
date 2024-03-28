package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class CreatureSpawnedEvent extends GameEvent {

    private final String creatureName;
    private final ICreature creature;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String creatureName;
        private ICreature creature;

        protected Builder() {
            super(GameEventType.SPAWN);
        }

        public String getCreatureName() {
            return creatureName;
        }

        public Builder setCreatureName(String creatureName) {
            this.creatureName = creatureName;
            return this;
        }

        public Builder setCreature(ICreature spawned) {
            this.creature = spawned;
            if (spawned != null) {
                this.creatureName = spawned.getName();
            }
            return this;
        }

        public ICreature getCreature() {
            return creature;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureSpawnedEvent Build() {
            return new CreatureSpawnedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureSpawnedEvent(Builder builder) {
        super(builder);
        this.creatureName = builder.getCreatureName();
        this.creature = builder.getCreature();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.creature != null) {
            myElement.appendChild(this.creature.buildXMLElement(nodeGenerator));
        } else if (creatureName != null) {
            myElement.appendChild(nodeGenerator.createTextNode(creatureName));
        } else {
            myElement.appendChild(nodeGenerator.createTextNode("Someone"));
        }
        myElement.appendChild(nodeGenerator.createTextNode(" has spawned in this room."));
        return myElement;
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public String getCreatureName() {
        return creatureName;
    }

    @Override
    public String printString() {
        return (creatureName != null ? creatureName : "Someone") + " has spawned in this room.";
    }
}
