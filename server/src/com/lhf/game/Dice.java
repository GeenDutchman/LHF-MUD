package com.lhf.game;

import java.util.List;
import java.util.Random;

public class Dice {
    private static Random rand = new Random();

    /**
     * Rolls so many of one type of die
     *
     * @param numDice how many dice to roll
     * @param dieType what type of die
     * @return the result of the roll
     */
    public static int roll(int numDice, int dieType) {
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
    public static int roll(List<Integer> dieNumbers, List<Integer> dieTypes) {
        int result = 0;

        //get the shortest size and use that
        int len = dieNumbers.size();
        if (dieTypes.size() < len) {
            len = dieTypes.size();
        }
        for (int i = 0; i < len; i++) {
            result += Dice.roll(dieNumbers.get(i), dieTypes.get(i));
        }
        return result;
    }
}
