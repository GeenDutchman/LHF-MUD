package com.lhf.game.dice;

public class DiceD2 extends Dice {

    public DiceD2(int count) {
        super(count, DieType.TWO);
    }

    @Override
    public int roll() {
        return DiceRoller.getInstance().d2(this.count);
    }

}