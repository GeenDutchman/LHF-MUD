package com.lhf.game.dice;

public class DiceD12 extends Dice {

    public DiceD12(int count) {
        super(count, DieType.TWELVE);
    }

    @Override
    protected int roll() {
        return DiceRoller.getInstance().d2(this.count);
    }

}