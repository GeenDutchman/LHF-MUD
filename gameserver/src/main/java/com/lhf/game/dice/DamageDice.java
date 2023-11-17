package com.lhf.game.dice;

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
