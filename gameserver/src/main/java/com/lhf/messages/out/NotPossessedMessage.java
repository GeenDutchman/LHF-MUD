package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class NotPossessedMessage extends OutMessage {
    private final String itemType;
    private final String itemName;
    private final Taggable found;

    public static class Builder extends OutMessage.Builder<Builder> {
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
        public NotPossessedMessage Build() {
            return new NotPossessedMessage(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public NotPossessedMessage(Builder builder) {
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
        if (this.found == null) {
            return "You do not have that " + this.itemType.toString() + " named '" + this.itemName.toString() + "'";
        }
        return this.found.getColorTaggedName() + " is not a " + this.itemType.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

}
