package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;

public class SpellFizzleMessage extends GameEvent {
    public enum SpellFizzleType {
        NOT_CASTER, BAD_POWER, NOT_SPELL, MISPRONOUNCE, OTHER;
    }

    private final SpellFizzleType subType;
    private final ICreature attempter;
    private final MultiRollResult offense;
    private final MultiRollResult defense;

    public static class Builder extends GameEvent.Builder<Builder> {
        private SpellFizzleType subType;
        private ICreature attempter;
        private MultiRollResult offense;
        private MultiRollResult defense;

        protected Builder() {
            super(GameEventType.FIZZLE);
        }

        public SpellFizzleType getSubType() {
            return subType;
        }

        public Builder setSubType(SpellFizzleType subType) {
            this.subType = subType;
            return this;
        }

        public ICreature getAttempter() {
            return attempter;
        }

        public Builder setAttempter(ICreature attempter) {
            this.attempter = attempter;
            return this;
        }

        public MultiRollResult getOffense() {
            return offense;
        }

        public Builder setOffense(MultiRollResult offense) {
            this.offense = offense;
            return this;
        }

        public MultiRollResult getDefense() {
            return defense;
        }

        public Builder setDefense(MultiRollResult defense) {
            this.defense = defense;
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

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpellFizzleMessage(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.attempter = builder.getAttempter();
        this.offense = builder.getOffense();
        this.defense = builder.getDefense();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.isBroadcast()) {
            if (this.attempter != null) {
                sj.add(this.attempter.getColorTaggedName());
            } else {
                sj.add("Someone");
            }
            sj.add(" mumbles and tries to cast a spell...nothing spectacular happens.");
            return sj.toString();
        }
        if (this.subType == null) {
            sj.add("Weird, that spell should have done something.");
        } else {
            switch (this.subType) {
                case NOT_CASTER:
                    sj.add("You are not a caster type, so you cannot cast spells.");
                case BAD_POWER:
                    sj.add("You have insufficient power to cast that spell.");
                case NOT_SPELL:
                    sj.add("That is not a spell that you can cast.");
                case MISPRONOUNCE:
                    sj.add("You did not invoke a spell properly");
                case OTHER:
                default:
                    sj.add("Weird, that spell should have done something.");
            }
        }
        if (this.offense != null) {
            sj.add("The attempt was so good:").add(this.offense.toString());
        }
        if (this.defense != null) {
            sj.add("The defense was like so:").add(this.defense.toString());
        }
        return sj.toString();
    }

    public SpellFizzleType getSubType() {
        return subType;
    }

    public ICreature getAttempter() {
        return attempter;
    }

    public MultiRollResult getOffense() {
        return offense;
    }

    public MultiRollResult getDefense() {
        return defense;
    }

    @Override
    public String print() {
        return this.toString();
    }

}
