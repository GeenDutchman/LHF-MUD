package com.lhf.messages.events;

import com.lhf.OutputBuilder;
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
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        OutputBuilder interaction = builder.produceSubBuilder("InteractionDescription");
        if (this.subType == null) {
            if (this.description == null || this.description.isBlank()) {
                interaction.appendString("Something happened because of the");
                if (this.taggable != null) {
                    interaction.appendTaggable(this.taggable, " ", ".");
                } else {
                    interaction.appendString("item", " ", ".");
                }
            } else {
                interaction.appendString(this.description);
            }
        }
        switch (this.subType) {
        case CANNOT:
            interaction.appendString("You try to interact with the");
            if (this.taggable != null) {
                interaction.appendTaggable(this.taggable);
            } else {
                interaction.appendString("item");
            }
            interaction.appendString(", but nothing happens.", null, null);
        case NO_METHOD:
            interaction.appendString("Weird, this");
            if (this.taggable != null) {
                interaction.appendTaggable(this.taggable);
            } else {
                interaction.appendString("item");
            }
            interaction.appendString("does nothing at all! It won't move!");
        case USED_UP:
            interaction.appendString("Nothing happened. It appears that the");
            if (this.taggable != null) {
                interaction.appendTaggable(this.taggable);
            } else {
                interaction.appendString("item");
            }
            interaction.appendString("has already been interacted with previously.");
        case ERROR:
            interaction
                    .appendString("You hear a weird grinding sound, and you assume that an error has occured with the");
            if (this.taggable != null) {
                interaction.appendTaggable(this.taggable);
            } else {
                interaction.appendString("item");
            }
            interaction.appendString("there.");

        case PERFORMED:
            // fallthrough
        default:
            if (this.description == null || this.description.isBlank()) {
                interaction.appendString("Something happened because of the");
                if (this.taggable != null) {
                    interaction.appendTaggable(this.taggable, " ", ".");
                } else {
                    interaction.appendString("item", " ", ".");
                }
            } else {
                interaction.appendString(this.description);
            }
        }
    }

}
