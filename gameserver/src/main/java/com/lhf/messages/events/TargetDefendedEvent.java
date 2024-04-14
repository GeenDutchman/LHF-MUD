package com.lhf.messages.events;

import com.lhf.OutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.GameEventType;

public class TargetDefendedEvent extends GameEvent {
    private final ICreature attacker;
    private final ICreature target;
    private final MultiRollResult offense;
    private final MultiRollResult defense;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature attacker;
        private ICreature target;
        private MultiRollResult offense;
        private MultiRollResult defense;

        protected Builder() {
            super(GameEventType.MISS);
        }

        public ICreature getAttacker() {
            return attacker;
        }

        public Builder setAttacker(ICreature attacker) {
            this.attacker = attacker;
            return this;
        }

        public ICreature getTarget() {
            return target;
        }

        public Builder setTarget(ICreature target) {
            this.target = target;
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
        public TargetDefendedEvent Build() {
            return new TargetDefendedEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public TargetDefendedEvent(Builder builder) {
        super(builder);
        this.attacker = builder.getAttacker();
        this.target = builder.getTarget();
        this.offense = builder.getOffense();
        this.defense = builder.getDefense();
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        Dice chooser = new DiceD4(1);
        int which = chooser.rollDice().getRoll();
        switch (which) {
        case 1:
            builder.appendTaggable(attacker);
            if (this.offense != null) {
                builder.appendTaggable(offense);
            }
            builder.appendString("misses").appendTaggable(target);
            if (this.defense != null) {
                builder.appendTaggable(defense);
            }
            break;
        case 2:
            builder.appendTaggable(target).appendString("dodged");
            if (this.defense != null) {
                builder.appendTaggable(defense);
            }
            builder.appendString("the attack");
            if (this.offense != null) {
                builder.appendTaggable(offense);
            }
            builder.appendString("from").appendTaggable(attacker);
            break;
        case 3:
            builder.appendTaggable(attacker).appendString("whiffed");
            if (this.offense != null) {
                builder.appendTaggable(offense);
            }
            builder.appendString("their attack on").appendTaggable(target);
            if (this.defense != null) {
                builder.appendTaggable(defense);
            }
            break;
        default:
            builder.appendString("The attack");
            if (this.offense != null) {
                builder.appendTaggable(offense);
            }
            builder.appendString("by").appendTaggable(attacker).appendString("on").appendTaggable(target)
                    .appendString("does not land");
            if (this.defense != null) {
                builder.appendTaggable(offense);
            }
            break;

        }
        builder.appendString("\r\n");

    }

    public ICreature getAttacker() {
        return attacker;
    }

    public ICreature getTarget() {
        return target;
    }

    public MultiRollResult getOffense() {
        return offense;
    }

    public MultiRollResult getDefense() {
        return defense;
    }

}
