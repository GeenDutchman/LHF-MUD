package com.lhf.messages.events;

import java.util.StringJoiner;

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

    private String printItem() {
        return this.usable != null ? this.usable.getColorTaggedName() : "item";
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.subType == null) {
            sj.add(this.addressCreature(this.itemUser, true)).add("used this").add(
                    this.printItem() + (this.target != null ? "on " + this.target.getColorTaggedName() + "." : "."));
        } else {
            switch (this.subType) {
                case NO_USES:
                    sj.add("You cannot use this").add(this.printItem()).add("like that!");
                case USED_UP:
                    sj.add("This").add(this.printItem()).add("has been used up.");
                case REQUIRE_EQUIPPED:
                    sj.add("You need to have this").add(this.printItem())
                            .add("equipped in order to use it!");
                case OK:
                default:
                    sj.add(this.addressCreature(this.itemUser, true)).add("used this").add(this.printItem()
                            + (this.target != null ? "on " + this.target.getColorTaggedName() + "." : "."));
            }
        }
        if (this.message != null && !this.message.isBlank()) {
            sj.add(this.getMessage());
        }
        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }
}
