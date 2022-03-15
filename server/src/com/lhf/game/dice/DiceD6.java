package com.lhf.game.dice;

public class DiceD6 extends Dice {

    public DiceD6(int count) {
        super(count, DieType.SIX);
    }

    @Override
    public int roll() {
        return DiceRoller.getInstance().d6(this.count);
    }

}