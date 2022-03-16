package com.lhf.game.dice;

import java.util.List;
import java.util.Random;

public class DiceRoller {
    private static DiceRoller _instance = null;
    private Random rand;

    private DiceRoller() {
        this.rand = new Random();
    }

    public static DiceRoller getInstance() {
        if (DiceRoller._instance == null) {
            DiceRoller._instance = new DiceRoller();
        }
        return DiceRoller._instance;
    }

    /**
     * Rolls so many of one type of die
     *
     * @param numDice how many dice to roll
     * @param dieType what type of die
     * @return the result of the roll
     */
    public int roll(int numDice, DieType dieType) {
        if (dieType.getType() <= 0) {
            return 0;
        }
        int result = 1 * numDice;
        for (int i = 0; i < numDice; i++) {
            result += rand.nextInt(dieType.getType());
        }
        return result;
    }

    /**
     * Returns the result of many rolls.
     * <p>
     * If the dieNumbers or dieTypes are different sizes, this will use the smallest
     * size of the two.
     * For each pair dieNumbers[i] and dieType[i] a roll is made and tallied, then
     * returned.
     *
     * @param dieNumbers how many of the corresponding type of die to roll
     * @param dieTypes   the type of die to roll
     * @return the total of the roll
     */
    public int roll(List<Integer> dieNumbers, List<Integer> dieTypes) {
        int result = 0;

        // get the shortest size and use that
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
        return this.roll(numDice, DieType.HUNDRED);
    }

    public int d20(int numDice) {
        return this.roll(numDice, DieType.TWENTY);
    }

    public int d12(int numDice) {
        return this.roll(numDice, DieType.TWELVE);
    }

    public int d10(int numDice) {
        return this.roll(numDice, DieType.TEN);
    }

    public int d8(int numDice) {
        return this.roll(numDice, DieType.EIGHT);
    }

    public int d6(int numDice) {
        return this.roll(numDice, DieType.SIX);
    }

    public int d4(int numDice) {
        return this.roll(numDice, DieType.FOUR);
    }

    public int d2(int numDice) {
        return this.roll(numDice, DieType.TWO);
    }
}
