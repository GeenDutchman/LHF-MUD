package com.lhf.messages.out;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class DropOutMessage extends OutMessage {
    private final Taggable item;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Taggable item;

        protected Builder() {
            super(OutMessageType.DROP_OUT);
        }

        public Taggable getItem() {
            return item;
        }

        public Builder setItem(Taggable item) {
            this.item = item;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public OutMessage Build() {
            return new DropOutMessage(this);
        }

    }

    public DropOutMessage(Builder builder) {
        super(builder);
        this.item = builder.getItem();
    }

    @Override
    public String toString() {
        return "You glance at your empty hand as the " + (this.item != null ? this.item.getColorTaggedName() : "item")
                + " drops to the floor.";
    }

    public Taggable getItem() {
        return item;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
