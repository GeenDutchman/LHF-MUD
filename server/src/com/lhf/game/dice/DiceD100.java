package com.lhf.game.dice;

public class DiceD100 extends Dice {

    public DiceD100(int count) {
        super(count, DieType.HUNDRED);
    }

    @Override
    protected int roll() {
        return DiceRoller.getInstance().d100(this.count);
    }

}