package com.lhf.game.dice;

public class DiceD10 extends Dice {

    public DiceD10(int count) {
        super(count, DieType.TEN);
    }

    @Override
    protected int roll() {
        return DiceRoller.getInstance().d10(this.count);
    }

}