package com.lhf.messages.grammar;

import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

public enum Prepositions {
    AT, TO, WITH, IN, USE, FROM, AS, ON;

    public static Set<String> asStringSet() {
        Set<String> preps = new TreeSet<>();
        for (Prepositions prep : Prepositions.values()) {
            preps.add(prep.toString().toLowerCase());
        }
        return preps;
    }

    public static EnumSet<Prepositions> asEnumSet() {
        return EnumSet.allOf(Prepositions.class);
    }

    public static Prepositions getPreposition(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }
        final String trimmedWord = word.trim();
        for (final Prepositions prep : Prepositions.values()) {
            if (trimmedWord.equalsIgnoreCase(prep.toString())) {
                return prep;
            }
        }
        return null;
    }

    public static boolean isPreposition(String word) {
        return Prepositions.getPreposition(word) != null;
    }
}
