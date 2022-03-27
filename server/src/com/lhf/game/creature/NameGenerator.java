package com.lhf.game.creature;

import java.util.Arrays;
import java.util.List;

import com.lhf.game.dice.DiceRoller;
import com.lhf.game.dice.DieType;

public class NameGenerator {
    private static List<String> suffixes = Arrays.asList("the Kinder", "the Snatcher", "Hardnibbler", "Bonecruncher",
            "FuZ", "the Third", "the Seventh", "Ohm", ", Mr.", "Manhunter", "Melonz", "Workit", "Shakeit");

    public static String Generate(String currName) {
        DiceRoller dRoller = DiceRoller.getInstance();
        Integer result = dRoller.roll(1, DieType.HUNDRED);
        String suffix = NameGenerator.suffixes.get(result % NameGenerator.suffixes.size());
        return currName.trim() + " " + suffix.trim();
    }
}
