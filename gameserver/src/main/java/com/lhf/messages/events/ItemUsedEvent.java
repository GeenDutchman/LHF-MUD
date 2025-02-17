package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;

import java.util.function.Consumer;

import com.lhf.RichOutput;
import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Usable;
import com.lhf.messages.GameEventType;

public class ItemUsedEvent extends GameEvent {
    public enum UseOutMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED, CANNOT;
    }

    private final UseOutMessageOption subType;
    private final ICreature itemUser;
    private final IItem usable;
    private final Taggable target;
    private final RichOutput message;

    public static class Builder extends GameEvent.Builder<Builder> {
        private UseOutMessageOption subType;
        private ICreature itemUser;
        private IItem usable;
        private Taggable target;
        private RichOutputBuilder message;

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

        public IItem getUsable() {
            return usable;
        }

        public Builder setUsable(IItem usable) {
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

        public RichOutputBuilder getMessageBuilder() {
            if (this.message == null) {
                this.message = new RichOutputBuilder();
            }
            return message;
        }

        public RichOutput getMessage() {
            return this.message != null ? this.message.build() : null;
        }

        public Builder setMessage(RichOutputBuilder messageBuilder) {
            this.message = messageBuilder;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = new RichOutputBuilder().appendChild(message);
            return this;
        }

        public Builder addMessage(String message) {
            this.getMessageBuilder().appendString(message);
            return this;
        }

        /**
         * Allow for the inline building of the message
         * 
         * @param messageEditor
         * @return
         */
        public Builder editMessage(Consumer<RichOutputBuilder> messageEditor) {
            if (messageEditor != null) {
                messageEditor.accept(this.getMessageBuilder());
            }
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

    public IItem getUsable() {
        return usable;
    }

    public Taggable getTarget() {
        return target;
    }

    public RichOutput getMessage() {
        return message;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.subType == null) {
            this.addressCreature(builder, itemUser);
            builder.appendString("used this");
            if (this.usable != null) {
                builder.appendTaggable(this.usable);
            } else {
                builder.appendString("item");
            }
            if (this.target != null) {
                builder.appendString("on");
                builder.appendTaggable(this.target);
            }
            builder.appendString(".", null, null);
        } else {
            switch (this.subType) {
            case NO_USES:
                builder.appendString("You cannot use this");
                if (this.usable != null) {
                    builder.appendTaggable(this.usable);
                } else {
                    builder.appendString("item");
                }
                builder.appendString("like that!");
                break;
            case USED_UP:
                builder.appendString("This");
                if (this.usable != null) {
                    builder.appendTaggable(this.usable);
                } else {
                    builder.appendString("item");
                }
                builder.appendString("has been used up.");
                break;
            case REQUIRE_EQUIPPED:
                builder.appendString("You need to have this");
                if (this.usable != null) {
                    builder.appendTaggable(this.usable);
                } else {
                    builder.appendString("item");
                }
                builder.appendString("equipped in order to use it!");
                break;
            case CANNOT:
                builder.appendString("Something prevents you from using this");
                if (this.usable != null) {
                    builder.appendTaggable(this.usable);
                } else {
                    builder.appendString("item");
                }
                builder.appendString(".", null, null);
                break;
            case OK:
            default:
                this.addressCreature(builder, itemUser);
                builder.appendString("used this");
                if (this.usable != null) {
                    builder.appendTaggable(this.usable);
                } else {
                    builder.appendString("item");
                }
                if (this.target != null) {
                    builder.appendString("on");
                    builder.appendTaggable(this.target);
                }
                builder.appendString(".", null, null);
            }
        }
        if (this.message != null) {
            builder.appendRichOutput(message);
        }
    }

}
