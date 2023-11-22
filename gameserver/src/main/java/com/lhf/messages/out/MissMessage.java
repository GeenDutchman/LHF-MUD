package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.OutMessageType;

public class MissMessage extends OutMessage {
    private final Creature attacker;
    private final Creature target;
    private final MultiRollResult offense;
    private final MultiRollResult defense;

    public static class Builder extends OutMessage.Builder<Builder> {
        private Creature attacker;
        private Creature target;
        private MultiRollResult offense;
        private MultiRollResult defense;

        protected Builder() {
            super(OutMessageType.MISS);
        }

        public Creature getAttacker() {
            return attacker;
        }

        public Builder setAttacker(Creature attacker) {
            this.attacker = attacker;
            return this;
        }

        public Creature getTarget() {
            return target;
        }

        public Builder setTarget(Creature target) {
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
        public MissMessage Build() {
            return new MissMessage(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public MissMessage(Builder builder) {
        super(builder);
        this.attacker = builder.getAttacker();
        this.target = builder.getTarget();
        this.offense = builder.getOffense();
        this.defense = builder.getDefense();
    }

    @Override
    public String toString() {
        StringJoiner output = new StringJoiner(" ");
        Dice chooser = new DiceD4(1);
        int which = chooser.rollDice().getRoll();
        switch (which) {
            case 1:
                output.add(attacker.getColorTaggedName());
                if (this.offense != null) {
                    output.add(this.offense.getColorTaggedName());
                }
                output.add("misses").add(target.getColorTaggedName());
                if (this.defense != null) {
                    output.add(this.defense.getColorTaggedName());
                }
                break;
            case 2:
                output.add(target.getColorTaggedName()).add("dodged");
                if (this.defense != null) {
                    output.add(this.defense.getColorTaggedName());
                }
                output.add("the attack");
                if (this.offense != null) {
                    output.add(this.offense.getColorTaggedName());
                }
                output.add("from").add(attacker.getColorTaggedName());
                break;
            case 3:
                output.add(attacker.getColorTaggedName()).add("whiffed");
                if (this.offense != null) {
                    output.add(this.offense.getColorTaggedName());
                }
                output.add("their attack on").add(target.getColorTaggedName());
                if (this.defense != null) {
                    output.add(this.defense.getColorTaggedName());
                }
                break;
            default:
                output.add("The attack");
                if (this.offense != null) {
                    output.add(this.offense.getColorTaggedName());
                }
                output.add("by").add(attacker.getColorTaggedName());
                output.add("on").add(target.getColorTaggedName());
                output.add("does not land");
                if (this.defense != null) {
                    output.add(this.defense.getColorTaggedName());
                }
                break;

        }
        output.add("\n");
        return output.toString();
    }

    public Creature getAttacker() {
        return attacker;
    }

    public Creature getTarget() {
        return target;
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
