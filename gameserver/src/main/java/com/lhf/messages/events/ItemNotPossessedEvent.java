package com.lhf.messages.events;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class ItemNotPossessedEvent extends GameEvent {
    private final String itemType;
    private final String itemName;
    private final Taggable found;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String itemType;
        private String itemName;
        private Taggable found;

        protected Builder() {
            super(GameEventType.NOT_POSSESSED);
        }

        public String getItemType() {
            return itemType;
        }

        public Builder setItemType(String type) {
            this.itemType = type;
            return this;
        }

        public String getItemName() {
            return itemName;
        }

        public Builder setItemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Taggable getFound() {
            return found;
        }

        public Builder setFound(Taggable found) {
            this.found = found;
            return this;
        }

        @Override
        public ItemNotPossessedEvent Build() {
            return new ItemNotPossessedEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemNotPossessedEvent(Builder builder) {
        super(builder);
        this.itemType = builder.getItemType();
        this.itemName = builder.getItemName();
        this.found = builder.getFound();
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public Taggable getFound() {
        return found;
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        if (this.found == null) {
            myElement.appendChild(nodeGenerator
                    .createTextNode(String.format("You do not have that %s named %s.", this.itemType, this.itemName)));
        } else {
            myElement.appendChild(this.found.buildXMLElement(nodeGenerator));
            myElement.appendChild(nodeGenerator.createTextNode(String.format(" is not a %s", this.itemType)));
        }
        return myElement;
    }

    @Override
    public String printString() {
        if (this.found == null) {
            return "You do not have that " + this.itemType.toString() + " named '" + this.itemName.toString() + "'";
        }
        return this.found.getSimpleContent() + " is not a " + this.itemType.toString();
    }

}
