package com.lhf.messages.events;

import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class ItemInteractionEvent extends GameEvent {
    public enum InteractOutMessageType {
        PERFORMED, CANNOT, NO_METHOD, USED_UP, ERROR;
    }

    private final Taggable taggable;
    private final InteractOutMessageType subType;
    private final String description;

    public static class Builder extends GameEvent.Builder<Builder> {
        private Taggable taggable;
        private InteractOutMessageType subType;
        private String description;

        protected Builder() {
            super(GameEventType.INTERACT);
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
        this.taggable = builder.getTaggable();
        this.subType = builder.getSubType();
        this.description = builder.getDescription();
    }

    private String enTag(String body) {
        return "<interaction>" + body + "</interaction>";
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.subType == null) {
            if (this.description == null || this.description.isBlank()) {
                sj.add("Something happened because of the");
                if (this.taggable != null) {
                    sj.add(this.taggable.getColorTaggedName());
                } else {
                    sj.add("item");
                }
                sj.add(".");
            } else {
                sj.add(this.description);
            }
            return this.enTag(sj.toString());
        }
        switch (this.subType) {
            case CANNOT:
                return this.enTag(sj.add("You try to interact with the")
                        .add(this.taggable != null ? this.taggable.getColorTaggedName() : "item")
                        .add(", but nothing happens.").toString());
            case NO_METHOD:
                return this.enTag(
                        sj.add("Weird, this").add(this.taggable != null ? this.taggable.getColorTaggedName() : "thing")
                                .add("does nothing at all!  It won't move!").toString());
            case USED_UP:
                return this
                        .enTag(sj.add("Nothing happened.  It appears that the")
                                .add(this.taggable != null ? this.taggable.getColorTaggedName() : "item")
                                .add("has already been interacted with previously.").toString());
            case ERROR:
                return this.enTag(
                        sj.add("You hear a weird grinding sound, and you assume that an error has occured with the")
                                .add(this.taggable != null ? this.taggable.getColorTaggedName() : "thingy")
                                .add("there.").toString());
            case PERFORMED:
                // fallthrough
            default:
                if (this.description == null || this.description.isBlank()) {
                    sj.add("Something happened because of the");
                    if (this.taggable != null) {
                        sj.add(this.taggable.getColorTaggedName());
                    } else {
                        sj.add("item");
                    }
                    sj.add(".");
                } else {
                    sj.add(this.description);
                }
                return this.enTag(sj.toString());
        }
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
    public String print() {
        return this.toString();
    }

}
