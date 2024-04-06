package com.lhf.messages.events;

import com.lhf.OutputBuilder;
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.found == null) {
            builder.appendString(String.format("You do not have that %s named %s.", this.itemType, this.itemName));
        } else {
            builder.appendTaggable(this.found).appendString(String.format("is not a %s", this.itemType));
        }
    }

}
