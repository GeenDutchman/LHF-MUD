package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.game.item.Item;
import com.lhf.messages.GameEventType;

public class ItemTakenEvent extends GameEvent {
    public enum TakeOutType {
        FOUND_TAKEN, NOT_FOUND, SHORT, INVALID, GREEDY, NOT_TAKEABLE, UNCLEVER, BAD_CONTAINER, LOCKED_CONTAINER;
    }

    private final String attemptedName;
    private final Item item;
    private final TakeOutType subType;
    private final String source;

    public static class Builder extends GameEvent.Builder<Builder> {
        private String attemptedName;
        private Item item;
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

        public Item getItem() {
            return item;
        }

        public Builder setItem(Item item) {
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

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.subType == null) {
            sj.add("You tried to take an item.");
            if (this.attemptedName != null) {
                sj.add("You tried to find it using the name:").add(this.attemptedName).add(".");
            }
            if (this.item != null) {
                sj.add("You found this item:").add(this.item.getColorTaggedName());
            }
            return sj.toString();
        }
        switch (this.subType) {
            case FOUND_TAKEN:
                if (this.item != null) {
                    sj.add(this.item.getColorTaggedName());
                } else {
                    sj.add("Item");
                }
                sj.add("successfully taken");
                if (this.source != null) {
                    sj.add("from").add(this.source);
                }
                sj.add("\n");
                return sj.toString();
            case NOT_FOUND:
                sj.add("Could not find that item");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                }
                sj.add("in");
                if (this.source != null) {
                    sj.add(this.source + ".");
                } else {
                    sj.add("this room.");
                }
                sj.add("\n");
                return sj.toString();
            case SHORT:
                sj.add("You'll need to be more specific than");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                sj.add("!\n");
                return sj.toString();
            case INVALID:
                sj.add("I don't think");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                sj.add("is a valid name\n");
                return sj.toString();
            case GREEDY:
                sj.add("Aren't you being a bit greedy there by trying to grab");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                sj.add("?\n");
                return sj.toString();
            case NOT_TAKEABLE:
                sj.add("That's strange--it's stuck in its place. You can't take the");
                if (this.item != null) {
                    sj.add(this.item.getColorTaggedName());
                } else {
                    sj.add("item");
                }
                sj.add("\n");
                return sj.toString();
            case UNCLEVER:
                sj.add("Are you trying to be too clever with");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                sj.add("?\n");
                return sj.toString();
            case BAD_CONTAINER:
                sj.add("You attempted to take");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                if (this.source != null) {
                    sj.add("from an unrecognized container or source: " + this.source + ".");
                } else {
                    sj.add("from an unrecognized container or source.");
                }
                sj.add("\n");
                return sj.toString();
            case LOCKED_CONTAINER:
                sj.add("You attempted to take");
                if (this.attemptedName != null) {
                    sj.add("'" + this.attemptedName + "'");
                } else {
                    sj.add("that");
                }
                sj.add("from");
                if (this.source != null) {
                    sj.add(this.source);
                } else {
                    sj.add("some container");
                }
                sj.add("but it is locked.");
                sj.add("\n");
                return sj.toString();
            default:
                sj.add("You tried to take an item.");
                if (this.attemptedName != null) {
                    sj.add("You tried to find it using the name:").add(this.attemptedName).add(".");
                }
                if (this.item != null) {
                    sj.add("You found this item:").add(this.item.getColorTaggedName());
                }
                return sj.toString();
        }
    }

    public String getAttemptedName() {
        return this.attemptedName;
    }

    public Item getItem() {
        return this.item;
    }

    public TakeOutType getSubType() {
        return this.subType;
    }

    @Override
    public String print() {
        return this.toString();
    }
}
