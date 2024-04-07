package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.Usable;
import com.lhf.messages.GameEventType;

public class ItemUsedEvent extends GameEvent {
    public enum UseOutMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED;
    }

    private final UseOutMessageOption subType;
    private final ICreature itemUser;
    private final Usable usable;
    private final Taggable target;
    private final String message;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UseOutMessageOption subType;
        private ICreature itemUser;
        private Usable usable;
        private Taggable target;
        private String message;

        protected Builder() {
            super(GameEventType.USE);
        }

        public UseOutMessageOption getSubType() {
            return subType;
        }

        public Builder setSubType(UseOutMessageOption subType) {
            this.subType = subType;
            return this;
        }

        public ICreature getItemUser() {
            return itemUser;
        }

        public Builder setItemUser(ICreature itemUser) {
            this.itemUser = itemUser;
            return this;
        }

        public Usable getUsable() {
            return usable;
        }

        public Builder setUsable(Usable usable) {
            this.usable = usable;
            return this;
        }

        public Taggable getTarget() {
            return target;
        }

        public Builder setTarget(Taggable target) {
            this.target = target;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemUsedEvent Build() {
            return new ItemUsedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemUsedEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.itemUser = builder.getItemUser();
        this.usable = builder.getUsable();
        this.target = builder.getTarget();
        this.message = builder.getMessage();
    }

    public UseOutMessageOption getSubType() {
        return subType;
    }

    public ICreature getItemUser() {
        return itemUser;
    }

    public Usable getUsable() {
        return usable;
    }

    public Taggable getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
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
        if (this.subType == null) {
            this.addressCreature(builder, itemUser);
            builder.appendString(" used this ");
            if (this.usable != null) {
                builder.appendTaggable(this.usable);
            } else {
                builder.appendString("item");
            }
            if (this.target != null) {
                builder.appendString(" on ");
                builder.appendTaggable(this.target);
            }
            builder.appendString(".", null, null);
        } else {
            switch (this.subType) {
                case NO_USES:
                    builder.appendString("You cannot use this ");
                    if (this.usable != null) {
                        builder.appendTaggable(this.usable);
                    } else {
                        builder.appendString("item");
                    }
                    builder.appendString(" like that!");
                    break;
                case USED_UP:
                    builder.appendString("This ");
                    if (this.usable != null) {
                        builder.appendTaggable(this.usable);
                    } else {
                        builder.appendString("item");
                    }
                    builder.appendString(" has been used up.");
                    break;
                case REQUIRE_EQUIPPED:
                    builder.appendString("YOu need to have this ");
                    if (this.usable != null) {
                        builder.appendTaggable(this.usable);
                    } else {
                        builder.appendString("item");
                    }
                    builder.appendString(" equipped in order to use it!");
                    break;
                case OK:
                default:
                    this.addressCreature(builder, itemUser);
                    builder.appendString(" used this ");
                    if (this.usable != null) {
                        builder.appendTaggable(this.usable);
                    } else {
                        builder.appendString("item");
                    }
                    if (this.target != null) {
                        builder.appendString(" on ");
                        builder.appendTaggable(this.target);
                    }
                    builder.appendString(".", null, null);
            }
        }
        if (this.message != null && !this.message.isBlank()) {
            builder.appendString(this.message);
        }
    }

}
