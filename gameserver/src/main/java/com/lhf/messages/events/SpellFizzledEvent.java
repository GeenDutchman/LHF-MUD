package com.lhf.messages.events;

import com.lhf.RichOutput;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;

public class SpellFizzledEvent extends GameEvent {
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
        public SpellFizzledEvent Build() {
            return new SpellFizzledEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public SpellFizzledEvent(Builder builder) {
        super(builder);
        this.subType = builder.getSubType();
        this.attempter = builder.getAttempter();
        this.offense = builder.getOffense();
        this.defense = builder.getDefense();
    }

    @Override
    public void buildOutput(RichOutput builder) {
        if (builder == null) {
            return;
        }
        if (this.isBroadcast()) {
            if (this.attempter != null) {
                builder.appendTaggable(attempter);
            } else {
                builder.appendString("Someone");
            }
            builder.appendString(" mumbles and tries to cast a spell...nothing spectacular happens.");
        }
        if (this.subType == null) {
            builder.appendString("Weird, that spell should have done something.");
        } else {
            switch (this.subType) {
            case NOT_CASTER:
                builder.appendString("You are not a caster type, so you cannot cast spells.");
            case BAD_POWER:
                builder.appendString("You have insufficient power to cast that spell.");
            case NOT_SPELL:
                builder.appendString("That is not a spell that you can cast.");
            case MISPRONOUNCE:
                builder.appendString("You did not invoke a spell properly");
            case OTHER:
            default:
                builder.appendString("Weird, that spell should have done something.");
            }
        }
        if (this.offense != null) {
            builder.appendString("The attempt was so good:").appendTaggable(this.offense);
        }
        if (this.defense != null) {
            builder.appendString("The defense was like so:").appendTaggable(this.defense);
        }
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

}
