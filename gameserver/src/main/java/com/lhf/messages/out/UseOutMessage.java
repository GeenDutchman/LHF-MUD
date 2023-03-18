package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.Usable;
import com.lhf.messages.OutMessageType;

public class UseOutMessage extends OutMessage {
    public enum UseOutMessageOption {
        OK, USED_UP, NO_USES, REQUIRE_EQUIPPED;
    }

    private final UseOutMessageOption subType;
    private final Creature itemUser;
    private final Usable usable;
    private final Taggable target;
    private final String message;

    public static class Builder extends OutMessage.Builder<Builder> {
        private UseOutMessageOption subType;
        private Creature itemUser;
        private Usable usable;
        private Taggable target;
        private String message;

        protected Builder() {
            super(OutMessageType.USE);
        }

        public UseOutMessageOption getSubType() {
            return subType;
        }

        public Builder setSubType(UseOutMessageOption subType) {
            this.subType = subType;
            return this;
        }

        public Creature getItemUser() {
            return itemUser;
        }

        public Builder setItemUser(Creature itemUser) {
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
        public UseOutMessage Build() {
            return new UseOutMessage(this);
        }

    }

    public UseOutMessage(Builder builder) {
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

    public Creature getItemUser() {
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
        StringJoiner sj = new StringJoiner(" ");
        switch (this.subType) {
            case NO_USES:
                sj.add("You cannot use this").add(this.usable.getColorTaggedName()).add("like that!");
            case USED_UP:
                sj.add("This").add(this.usable.getColorTaggedName()).add("has been used up.");
            case REQUIRE_EQUIPPED:
                sj.add("You need to have this").add(this.usable.getColorTaggedName())
                        .add("equipped in order to use it!");
            case OK:
            default:
                sj.add("You used").add(this.usable.getColorTaggedName()).add(".");
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
