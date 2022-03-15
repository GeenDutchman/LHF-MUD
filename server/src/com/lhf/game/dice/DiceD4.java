package com.lhf.game.dice;

public class DiceD4 extends Dice {

    public DiceD4(int count) {
        super(count, DieType.FOUR);
    }

    @Override
    public int roll() {
        return DiceRoller.getInstance().d4(this.count);
    }

}