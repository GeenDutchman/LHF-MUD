package com.lhf.game.creature;

import java.util.Arrays;
import java.util.List;

import com.lhf.game.dice.DiceRoller;
import com.lhf.game.dice.DieType;

public class NameGenerator {
    private static List<String> givensnames = Arrays.asList("Serlio", "Mangaka", "Toohru", "Zappy", "Pleenpleen",
            "Jacuz", "Narly", "Biggy", "Naaaman", "Rekt", "Poofy", "Hurg", "Nurg", "Lekker",
            "Trumble", "Arianna", "Taylor", "Daddy", "Shaman", "Sarre", "Ninina", "Jalpina", "Lost", "Zoomy", "Mister");
    private static List<String> suffixes = Arrays.asList("the Kinder", "the Snatcher", "Hardnibbler", "Bonecruncher",
            "FuZ", "the Third", "the Seventh", "Ohm", "the Mister", "Manhunter", "Melonz", "Workit", "Shakeit",
            "Nightdeath");

    public static String GenerateGiven() {
        DiceRoller dRoller = DiceRoller.getInstance();
        Integer result = dRoller.roll(1, DieType.HUNDRED);
        String givenname = NameGenerator.givensnames.get(result % NameGenerator.givensnames.size());
        return givenname;
    }

    public static String GenerateSuffix(String currName) {
        DiceRoller dRoller = DiceRoller.getInstance();
        Integer result = dRoller.roll(1, DieType.HUNDRED);
        String suffix = NameGenerator.suffixes.get(result % NameGenerator.suffixes.size());
        return currName.trim() + " " + suffix.trim();
    }
}
