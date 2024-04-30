package com.lhf.messages.events;

import com.lhf.Examinable;
import com.lhf.RichOutput;
import com.lhf.game.item.IItem;
import com.lhf.messages.GameEventType;

public class ItemTakenEvent extends GameEvent {
    public enum TakeOutType {
        FOUND_TAKEN, NOT_FOUND, SHORT, INVALID, GREEDY, NOT_TAKEABLE, UNCLEVER, BAD_CONTAINER, LOCKED_CONTAINER;
    }

    private final String attemptedName;
    private final IItem item;
    private final TakeOutType subType;
    private final String source;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String attemptedName;
        private IItem item;
        private TakeOutType subType;
        private String source;

        protected Builder() {
            super(GameEventType.TAKE);
        }

        public String getAttemptedName() {
            return attemptedName;
        }

        public Builder setAttemptedName(String attemptedName) {
            this.attemptedName = attemptedName;
            return this;
        }

        public IItem getItem() {
            return item;
        }

        public Builder setItem(IItem item) {
            this.item = item;
            return this;
        }

        public TakeOutType getSubType() {
            return subType;
        }

        public Builder setSubType(TakeOutType subType) {
            this.subType = subType;
            return this;
        }

        public String getSource() {
            return source;
        }

        public Builder setSource(Examinable source) {
            this.source = source.getName();
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemTakenEvent Build() {
            return new ItemTakenEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemTakenEvent(Builder builder) {
        super(builder);
        this.item = builder.getItem();
        this.attemptedName = builder.getAttemptedName();
        this.subType = builder.getSubType();
        this.source = builder.getSource();
    }

    public String getAttemptedName() {
        return this.attemptedName;
    }

    public IItem getItem() {
        return this.item;
    }

    public TakeOutType getSubType() {
        return this.subType;
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.subType == null) {
            builder.appendString("You tried to take an item.");
            if (this.attemptedName != null) {
                builder.appendString(String.format("You tried to take it using the name: %s. ", this.attemptedName));
            }
            if (this.item != null) {
                builder.appendString("You found this item:").appendTaggable(item);
            }
            return;
        }
        switch (this.subType) {
        case FOUND_TAKEN:
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("Item");
            }
            builder.appendString(
                    String.format(" successfully taken%s.", this.source != null ? " from " + this.source : ""));

            return;
        case NOT_FOUND:
            builder.appendString(String.format("Could not find that item %sin %s.",
                    this.attemptedName != null ? "'" + this.attemptedName + "' " : "",
                    this.source != null ? this.source : "this room"));

            return;
        case SHORT:
            builder.appendString(String.format("You'll need to be more specific than %s!",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that"));

            return;
        case INVALID:
            builder.appendString(String.format("I don't think %s is a valid name.",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that"));
            return;
        case GREEDY:
            builder.appendString(String.format("Aren't you being a bit greedy there by trying to grab %s?",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that"));
            return;

        case NOT_TAKEABLE:
            builder.appendString("That's strange--it's stuck in its place. You can't take the");
            if (this.item != null) {
                builder.appendTaggable(item);
            } else {
                builder.appendString("item;");
            }
            return;
        case UNCLEVER:
            builder.appendString(String.format("Are you trying to be too clever with %s?",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that"));
            return;

        case BAD_CONTAINER:
            builder.appendString(String.format("You attempted to take %s from an unrecognized container or source%s.",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that",
                    this.source != null ? this.source : ""));
            return;

        case LOCKED_CONTAINER:
            builder.appendString(String.format("You attempted to take %s from %s but it is locked.",
                    this.attemptedName != null ? "'" + this.attemptedName + "'" : "that",
                    this.source != null ? this.source : "some container"));
            return;

        default:
            builder.appendString("You tried to take an item.");
            if (this.attemptedName != null) {
                builder.appendString(String.format("You tried to take it using the name: %s. ", this.attemptedName));
            }
            if (this.item != null) {
                builder.appendString("You found this item:").appendTaggable(item);
            }
            return;
        }
    }

}
