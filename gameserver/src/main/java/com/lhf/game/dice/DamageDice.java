package com.lhf.game.dice;

import com.lhf.game.enums.DamageFlavor;

public class DamageDice extends Dice {
    public class FlavoredRollResult extends RollResult {
        public FlavoredRollResult(int total) {
            super(total);
        }

        public DamageFlavor getFlavor() {
            return DamageDice.this.getFlavor();
        }

    }

    public DamageDice(int count, DieType type, DamageFlavor flavor) {
        super(count, type);
        this.flavor = flavor;
    }

    private DamageFlavor flavor;

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

    public DamageFlavor getFlavor() {
        return flavor;
    }

}
