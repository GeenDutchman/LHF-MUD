package com.lhf.game.dice;

import java.util.List;
import java.util.Random;

public class Dice {
    private static Dice _instance = null;
    private Random rand;
    private final int HUNDRED = 100;
    private final int TWENTY = 20;
    private final int TWELVE = 12;
    private final int TEN = 10;
    private final int EIGHT = 8;
    private final int SIX = 6;
    private final int FOUR = 4;
    private final int TWO = 2; // coin

    private Dice() {
        this.rand = new Random();
    }

    public static Dice getInstance() {
        if (Dice._instance == null) {
            Dice._instance = new Dice();
        }
        return Dice._instance;
    }

    /**
     * Rolls so many of one type of die
     *
     * @param numDice how many dice to roll
     * @param dieType what type of die
     * @return the result of the roll
     */
    private int roll(int numDice, int dieType) {
        if (dieType <= 0) {
            return 0;
        }
        int result = 1;
        for (int i = 0; i < numDice; i++) {
            result += rand.nextInt(dieType);
        }
        return result;
    }

    /**
     * Returns the result of many rolls.
     * <p>
     * If the dieNumbers or dieTypes are different sizes, this will use the smallest size of the two.
     * For each pair dieNumbers[i] and dieType[i] a roll is made and tallied, then returned.
     *
     * @param dieNumbers how many of the corresponding type of die to roll
     * @param dieTypes   the type of die to roll
     * @return the total of the roll
     */
    public int roll(List<Integer> dieNumbers, List<Integer> dieTypes) {
        int result = 0;

        //get the shortest size and use that
        int len = dieNumbers.size();
        if (dieTypes.size() < len) {
            len = dieTypes.size();
        }
        for (int i = 0; i < len; i++) {
            result += this.roll(dieNumbers.get(i), dieTypes.get(i));
        }
        return result;
    }

    public int d100(int numDice) {
        return this.roll(numDice, this.HUNDRED);
    }

    public int d20(int numDice) {
        return this.roll(numDice, this.TWENTY);
    }

    public int d12(int numDice) {
        return this.roll(numDice, this.TWELVE);
    }

    public int d10(int numDice) {
        return this.roll(numDice, this.TEN);
    }

    public int d8(int numDice) {
        return this.roll(numDice, this.EIGHT);
    }

    public int d6(int numDice) {
        return this.roll(numDice, this.SIX);
    }

    public int d4(int numDice) {
        return this.roll(numDice, this.FOUR);
    }

    public int d2(int numDice) {
        return this.roll(numDice, this.TWO);
    }
}
