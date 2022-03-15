package com.lhf.game.dice;

public class DiceD20 extends Dice {

    public DiceD20(int count) {
        super(count, DieType.TWENTY);
    }

    @Override
    public int roll() {
        return DiceRoller.getInstance().d20(this.count);
    }

}