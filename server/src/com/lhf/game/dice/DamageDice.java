package com.lhf.game.dice;

import com.lhf.game.enums.DamageFlavor;

public class DamageDice extends Dice {
    public DamageDice(int count, DieType type, DamageFlavor flavor) {
        super(count, type);
        this.flavor = flavor;
    }

    private DamageFlavor flavor;

    @Override
    public int roll() {
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
