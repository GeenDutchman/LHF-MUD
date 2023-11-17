package com.lhf.game.dice;

import java.util.function.IntUnaryOperator;

import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamageFlavor.DamageFlavored;

public class DamageDice extends Dice implements DamageFlavored {
    public class FlavoredRollResult extends RollResult implements DamageFlavored {
        public FlavoredRollResult(int total) {
            super(total);
        }

        @Override
        public DamageFlavor getDamageFlavor() {
            return DamageDice.this.getDamageFlavor();
        }

        @Override
        protected IRollResult annotate(IRollResult result, IntUnaryOperator operation, String note) {
            return new DamageDice.FlavoredAnnotatedRollResult(result, operation, note);
        }

    }

    public class FlavoredAnnotatedRollResult extends AnnotatedRollResult implements DamageFlavored {

        public FlavoredAnnotatedRollResult(IRollResult result, int alteredResult, String note) {
            super(result, alteredResult, note);
        }

        public FlavoredAnnotatedRollResult(final IRollResult result, final IntUnaryOperator operation,
                final String note) {
            super(result, operation, note);
        }

        @Override
        protected IRollResult annotate(IRollResult result, IntUnaryOperator operation, String note) {
            return new DamageDice.FlavoredAnnotatedRollResult(result, operation, note);
        }

        @Override
        public DamageFlavor getDamageFlavor() {
            return DamageDice.this.getDamageFlavor();
        }

    }

    public DamageDice(int count, DieType type, DamageFlavor flavor) {
        super(count, type);
        this.flavor = flavor;
    }

    private final DamageFlavor flavor;

    @Override
    public RollResult rollDice() {
        return new DamageDice.FlavoredRollResult(this.roll());
    }

    @Override
    protected int roll() {
        return DiceRoller.getInstance().roll(this.count, this.type);
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + flavor.toString();
    }

    @Override
    public DamageFlavor getDamageFlavor() {
        return flavor;
    }

}
