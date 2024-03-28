package com.lhf.messages.events;

import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.GameEventType;

public class ItemInteractionEvent extends GameEvent {
    public enum InteractOutMessageType {
        PERFORMED, CANNOT, NO_METHOD, USED_UP, ERROR;
    }

    private final ICreature interactor;
    private final Taggable taggable;
    private final InteractOutMessageType subType;
    private final String description;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature interactor;
        private Taggable taggable;
        private InteractOutMessageType subType;
        private String description;

        protected Builder() {
            super(GameEventType.INTERACT);
        }

        public ICreature getInteractor() {
            return this.interactor;
        }

        public Builder setInteractor(ICreature whodunnit) {
            this.interactor = whodunnit;
            return this;
        }

        public Taggable getTaggable() {
            return taggable;
        }

        public Builder setTaggable(Taggable taggable) {
            this.taggable = taggable;
            return this;
        }

        public InteractOutMessageType getSubType() {
            return subType;
        }

        public Builder setSubType(InteractOutMessageType subType) {
            this.subType = subType;
            return this;
        }

        public Builder setPerformed() {
            this.subType = InteractOutMessageType.PERFORMED;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemInteractionEvent Build() {
            return new ItemInteractionEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemInteractionEvent(Builder builder) {
        super(builder);
        this.interactor = builder.getInteractor();
        this.taggable = builder.getTaggable();
        this.subType = builder.getSubType();
        this.description = builder.getDescription();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public ICreature getInteractor() {
        return interactor;
    }

    public Taggable getTaggable() {
        return taggable;
    }

    public InteractOutMessageType getSubType() {
        return subType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        Element interaction = nodeGenerator.createElement("interaction");
        interaction.setAttribute("subtype",
                this.subType != null ? this.subType.toString() : InteractOutMessageType.PERFORMED.toString());
        myElement.appendChild(interaction);
        if (this.subType == null) {
            if (this.description == null || this.description.isBlank()) {
                interaction.appendChild(nodeGenerator.createTextNode("Something happened because of the "));
                if (this.taggable != null) {
                    interaction.appendChild(this.taggable.buildXMLElement(nodeGenerator));
                } else {
                    interaction.appendChild(nodeGenerator.createTextNode(" item"));
                }
                interaction.appendChild(nodeGenerator.createTextNode("."));
            } else {
                interaction.appendChild(nodeGenerator.createTextNode(this.description));
            }
            return myElement;
        }
        switch (this.subType) {
        case CANNOT:
            interaction.appendChild(nodeGenerator.createTextNode("You try to interact with the "));
            interaction.appendChild(this.taggable != null ? this.taggable.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode(" item "));
            interaction.appendChild(nodeGenerator.createTextNode(", but nothing happens."));
        case NO_METHOD:
            interaction.appendChild(nodeGenerator.createTextNode("Weird, this"));
            interaction.appendChild(this.taggable != null ? this.taggable.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode(" item "));
            interaction.appendChild(nodeGenerator.createTextNode("does nothing at all!  It won't move!"));
        case USED_UP:
            interaction.appendChild(nodeGenerator.createTextNode("Nothing happened.  It appears that the"));
            interaction.appendChild(this.taggable != null ? this.taggable.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode(" item "));
            interaction.appendChild(nodeGenerator.createTextNode("has already been interacted with previously."));
        case ERROR:
            interaction.appendChild(nodeGenerator.createTextNode(
                    "You hear a weird grinding sound, and you assume that an error has occured with the"));
            interaction.appendChild(this.taggable != null ? this.taggable.buildXMLElement(nodeGenerator)
                    : nodeGenerator.createTextNode(" item "));
            interaction.appendChild(nodeGenerator.createTextNode("there."));
        case PERFORMED:
            // fallthrough
        default:
            if (this.description == null || this.description.isBlank()) {
                interaction.appendChild(nodeGenerator.createTextNode("Something happened because of the "));
                if (this.taggable != null) {
                    interaction.appendChild(this.taggable.buildXMLElement(nodeGenerator));
                } else {
                    interaction.appendChild(nodeGenerator.createTextNode(" item"));
                }
                interaction.appendChild(nodeGenerator.createTextNode("."));
            } else {
                interaction.appendChild(nodeGenerator.createTextNode(this.description));
            }
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.subType == null) {
            if (this.description == null || this.description.isBlank()) {
                sj.add("Something happened because of the");
                if (this.taggable != null) {
                    sj.add(this.taggable.getSimpleContent());
                } else {
                    sj.add("item");
                }
                sj.add(".");
            } else {
                sj.add(this.description);
            }
            return sj.toString();
        }
        switch (this.subType) {
        case CANNOT:
            return sj.add("You try to interact with the")
                    .add(this.taggable != null ? this.taggable.getSimpleContent() : "item")
                    .add(", but nothing happens.").toString();
        case NO_METHOD:
            return sj.add("Weird, this").add(this.taggable != null ? this.taggable.getSimpleContent() : "thing")
                    .add("does nothing at all!  It won't move!").toString();
        case USED_UP:
            return sj.add("Nothing happened.  It appears that the")
                    .add(this.taggable != null ? this.taggable.getSimpleContent() : "item")
                    .add("has already been interacted with previously.").toString();
        case ERROR:
            return sj.add("You hear a weird grinding sound, and you assume that an error has occured with the")
                    .add(this.taggable != null ? this.taggable.getSimpleContent() : "thingy").add("there.").toString();
        case PERFORMED:
            // fallthrough
        default:
            if (this.description == null || this.description.isBlank()) {
                sj.add("Something happened because of the");
                if (this.taggable != null) {
                    sj.add(this.taggable.getSimpleContent());
                } else {
                    sj.add("item");
                }
                sj.add(".");
            } else {
                sj.add(this.description);
            }
            return sj.toString();
        }
    }

}
