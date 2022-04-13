package com.lhf.game.dice;

public class DiceDC extends Dice {

    protected Integer difficultyClass;

    public DiceDC(Integer dc) {
        super(0, DieType.NONE);
        this.difficultyClass = dc;
    }

    protected Integer getDC() {
        return this.difficultyClass;
    }

    @Override
    protected int roll() {
        return this.getDC();
    }

    @Override
    public String toString() {
        return "DC " + this.getDC().toString();
    }

}
