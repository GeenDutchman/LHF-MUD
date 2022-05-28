package com.lhf.game.dice;

public enum DieType {
    HUNDRED(100), TWENTY(20), TWELVE(12), TEN(10), EIGHT(8), SIX(6), FOUR(4), TWO(2), NONE(0); // coin

    private int type;

    DieType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public String toString() {
        if (this.type <= 0) {
            return "";
        }
        return "" + this.type;
    }

}