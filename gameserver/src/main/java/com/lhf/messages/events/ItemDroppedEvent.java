package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class ItemDroppedEvent extends GameEvent {
    private final Taggable item;
    private final String destination;
    private final DropType dropType;

    public enum DropType {
        SUCCESS, NO_ITEM, BAD_CONTAINER, LOCKED_CONTAINER
    }

    public static class Builder extends GameEvent.Builder<Builder> {
        private Taggable item;
        private String destination;
        private DropType dropType;

        protected Builder() {
            super(GameEventType.DROP_OUT);
        }

        public Taggable getItem() {
            return item;
        }

        public Builder setItem(Taggable item) {
            this.item = item;
            return this;
        }

        public String getDestination() {
            return destination;
        }

        public Builder setDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public DropType getDropType() {
            return dropType;
        }

        public Builder setDropType(DropType dropType) {
            this.dropType = dropType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemDroppedEvent Build() {
            return new ItemDroppedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemDroppedEvent(Builder builder) {
        super(builder);
        this.item = builder.getItem();
        this.destination = builder.getDestination();
        this.dropType = builder.getDropType();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public Taggable getItem() {
        return item;
    }

    public String getDestination() {
        return this.destination;
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.dropType == null) {
            builder.appendString("You glance at your empty hand as the");
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("item");
            }
            builder.appendString("drops to the floor");
            if (this.destination != null) {
                builder.appendString(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the");
                builder.appendString(destination);
            }
            return;
        }
        switch (this.dropType) {
        case BAD_CONTAINER:
            builder.appendString("You attempted to drop");
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("that");
            }
            builder.appendString(String.format(" into an unrecognized container or source%s.",
                    this.destination != null ? ":" + this.destination : ""));
            return;
        case LOCKED_CONTAINER:
            builder.appendString("You attempted to drop");
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("that");
            }

            String destName = "some container";
            if (this.destination != null) {
                if (this.destination.trim().toLowerCase().startsWith("the")) {
                    destName = "the '" + this.destination + "'";
                } else {
                    destName = "'" + this.destination + "'";
                }
            }
            builder.appendString(String.format("into %s but it is locked.", destName));

            return;
        case NO_ITEM:
            builder.appendString("You failed to name an item to drop.");
            return;
        case SUCCESS:
        default:
            builder.appendString("You glance at your empty hand as the");
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("item");
            }
            builder.appendString("drops to the floor");
            if (this.destination != null) {
                builder.appendString(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the");
                builder.appendString(destination);
            }
            return;
        }
    }

    public DropType getDropType() {
        return dropType;
    }
}
