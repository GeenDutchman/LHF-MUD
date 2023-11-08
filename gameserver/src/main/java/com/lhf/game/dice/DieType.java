package com.lhf.game.dice;

public enum DieType {
    NONE(0), TWO(2), FOUR(4), SIX(6), EIGHT(8), TEN(10), TWELVE(12), TWENTY(20), HUNDRED(100);

    public static DieType getDieType(String value) {
        for (DieType dType : values()) {
            if (dType.toString().equalsIgnoreCase(value)) {
                return dType;
            }
        }
        return null;
    }

    public static boolean isDieType(String value) {
        return DieType.getDieType(value) != null;
    }

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
