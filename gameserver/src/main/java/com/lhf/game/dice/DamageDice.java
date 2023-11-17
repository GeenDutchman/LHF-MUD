package com.lhf.game.dice;

import java.util.function.IntUnaryOperator;

import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamageFlavor.DamageFlavored;

public class DamageDice extends Dice implements DamageFlavored {
    public class FlavoredRollResult extends RollResult implements DamageFlavored {
        public FlavoredRollResult(int total) {
            super(total);
        }

        protected FlavoredRollResult(final RollResult result, final int alteredResult, final String note) {
            super(result, alteredResult, note);
        }

        @Override
        public DamageFlavor getDamageFlavor() {
            return DamageDice.this.getDamageFlavor();
        }

        @Override
        protected FlavoredRollResult annotate(IntUnaryOperator operation, String note) {
            if (operation == null) {
                return this;
            }
            return new DamageDice.FlavoredRollResult(this, operation.applyAsInt(this.roll), note);
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
