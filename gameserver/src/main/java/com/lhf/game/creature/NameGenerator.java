package com.lhf.game.creature;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.game.dice.DiceRoller;
import com.lhf.game.dice.DieType;

public class NameGenerator {
    private static List<String> givensnames = Arrays.asList("Serlio", "Mangaka", "Toohru", "Zappy", "Pleenpleen",
            "Jacuz", "Narly", "Biggy", "Naaaman", "Rekt", "Poofy", "Hurg", "Nurg", "Lekker",
            "Trumble", "Arianna", "Taylor", "Daddy", "Shaman", "Sarre", "Ninina", "Jalpina", "Lost", "Zoomy", "Mister",
            "EDGELORD", "Himeko", "Ingrid", "Joon", "Camber", "Kimber", "Hilder", "Loki");
    private static List<String> suffixes = Arrays.asList("the Kinder", "the Snatcher", "Hardnibbler", "Bonecruncher",
            "FuZ", "the Third", "the Seventh", "Ohm", "the Mister", "Manhunter", "Melonz", "Workit", "Shakeit",
            "Nightdeath", "Hugin", "Mugin", "Naaktgeboren", "Hireon", "Halfman", "Ostero", "Limbo", "THE EDGELORD",
            "Hayes", "Cupbearer", "Dattoken", "Dreammaker", "Rider", "Ysmir", "Tismet", "Qiin", "Smith");

    private static Set<String> namespace = new TreeSet<>();

    public static String Generate(String currName) {
        String name = null;
        do {
            name = currName == null || currName.isEmpty() || currName.isBlank() ? NameGenerator.GenerateGiven()
                    : currName;
            name = NameGenerator.GenerateSuffix(name);
        } while (NameGenerator.namespace.contains(name));
        NameGenerator.namespace.add(name);
        return name;
    }

    private static String GenerateGiven() {
        DiceRoller dRoller = DiceRoller.getInstance();
        Integer result = dRoller.roll(1, DieType.HUNDRED);
        String givenname = NameGenerator.givensnames.get(result % NameGenerator.givensnames.size());
        return givenname;
    }

    private static String GenerateSuffix(String currName) {
        DiceRoller dRoller = DiceRoller.getInstance();
        Integer result = dRoller.roll(1, DieType.HUNDRED);
        String suffix = NameGenerator.suffixes.get(result % NameGenerator.suffixes.size());
        return currName.trim() + " " + suffix.trim();
    }
}
