package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class DropOutMessage extends OutMessage {
    private final Taggable item;
    private final String destination;
    private final DropType dropType;

    public enum DropType {
        SUCCESS, NO_ITEM, BAD_CONTAINER, LOCKED_CONTAINER
    }

    public static class Builder extends OutMessage.Builder<Builder> {
        private Taggable item;
        private String destination;
        private DropType dropType;

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
        public DropOutMessage Build() {
            return new DropOutMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public DropOutMessage(Builder builder) {
        super(builder);
        this.item = builder.getItem();
        this.destination = builder.getDestination();
        this.dropType = builder.getDropType();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        switch (this.dropType) {
            case BAD_CONTAINER:
                sj.add("You attempted to drop");
                if (this.item != null) {
                    sj.add("'" + this.item.getColorTaggedName() + "'");
                } else {
                    sj.add("that");
                }
                if (this.destination != null) {
                    sj.add("into an unrecognized container or source: " + this.destination + ".");
                } else {
                    sj.add("into an unrecognized container or source.");
                }
                sj.add("\n");
                return sj.toString();
            case LOCKED_CONTAINER:
                sj.add("You attempted to drop");
                if (this.item != null) {
                    sj.add("'" + this.item.getColorTaggedName() + "'");
                } else {
                    sj.add("that");
                }
                sj.add("into");
                if (this.destination != null) {
                    sj.add(this.destination.trim().toLowerCase().startsWith("the") ? "'" + this.destination + "'"
                            : "the '" + this.destination + "'");
                } else {
                    sj.add("some container");
                }
                sj.add("but it is locked.");
                sj.add("\n");
                return sj.toString();
            case NO_ITEM:
                sj.add("You failed to name an item to drop.");
                return sj.toString();
            case SUCCESS:
            default:
                sj.add("You glance at your empty hand as the");
                if (this.item != null) {
                    sj.add(this.item.getColorTaggedName());
                } else {
                    sj.add("item");
                }
                sj.add("drops to the floor");
                if (this.destination != null) {
                    sj.add(this.destination.trim().toLowerCase().startsWith("the") ? "of" : "of the")
                            .add(this.destination);
                }
                return sj.toString() + ".";
        }
    }

    public Taggable getItem() {
        return item;
    }

    public String getDestination() {
        return this.destination;
    }

    @Override
    public String print() {
        return this.toString();
    }

    public DropType getDropType() {
        return dropType;
    }
}
