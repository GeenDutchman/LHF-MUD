package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.messages.OutMessageType;

public class SpellFizzleMessage extends OutMessage {
    public enum SpellFizzleType {
        NOT_CASTER, BAD_POWER, NOT_SPELL, MISPRONOUNCE, OTHER;
    }

    private final SpellFizzleType subType;
    private final Creature attempter;

    public static class Builder extends OutMessage.Builder<Builder> {
        private SpellFizzleType subType;
        private Creature attempter;

        protected Builder() {
            super(OutMessageType.FIZZLE);
        }

        public SpellFizzleType getSubType() {
            return subType;
        }

        public Builder setSubType(SpellFizzleType subType) {
            this.subType = subType;
            return this;
        }

        public Creature getAttempter() {
            return attempter;
        }

        public Builder setAttempter(Creature attempter) {
            this.attempter = attempter;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public SpellFizzleMessage Build() {
            return new SpellFizzleMessage(this);
        }

    }

    public SpellFizzleMessage(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.attempter = builder.getAttempter();
    }

    @Override
    public String toString() {
        if (this.isBroadcast()) {
            StringJoiner sj = new StringJoiner(" ");
            if (this.attempter != null) {
                sj.add(this.attempter.getColorTaggedName());
            } else {
                sj.add("Someone");
            }
            sj.add(" mumbles and tries to cast a spell...nothing spectacular happens.");
            return sj.toString();
        }
        switch (this.subType) {
            case NOT_CASTER:
                return "You are not a caster type, so you cannot cast spells.";
            case BAD_POWER:
                return "You have insufficient power to cast that spell.";
            case NOT_SPELL:
                return "That is not a spell that you can cast.";
            case MISPRONOUNCE:
                return "You did not invoke a spell properly";
            case OTHER:
            default:
                return "Weird, that spell should have done something.";
        }
    }

    public SpellFizzleType getSubType() {
        return subType;
    }

    public Creature getAttempter() {
        return attempter;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
