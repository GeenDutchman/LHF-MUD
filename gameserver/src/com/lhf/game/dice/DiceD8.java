package com.lhf.game.dice;

public class DiceD8 extends Dice {

    public DiceD8(int count) {
        super(count, DieType.EIGHT);
    }

    @Override
    protected int roll() {
        return DiceRoller.getInstance().d8(this.count);
    }

}