package com.lhf.messages.grammar;

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

    public static boolean isPreposition(String word) {
        if (word == null || word.isBlank()) {
            return false;
        }
        String trimmedWord = word.trim().toLowerCase();
        for (Prepositions prep : Prepositions.values()) {
            if (trimmedWord.equalsIgnoreCase(prep.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
