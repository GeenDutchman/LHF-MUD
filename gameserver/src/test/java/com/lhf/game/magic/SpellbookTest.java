package com.lhf.game.magic;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class SpellbookTest {
    @Test
    void testSaving() throws IOException {
        Spellbook spellbook = new Spellbook();
        Truth.assertThat(spellbook.saveToFile()).isTrue();
    }

    @Test
    void testLoading() {
        Spellbook spellbook = new Spellbook();
        ThirdPower thirdPower = new ThirdPower(null, spellbook);
        TreeMap<Integer, SortedSet<SpellEntry>> counts = new TreeMap<>();
        Integer presize = 0;
        for (Integer i = 0; i < 11; i++) {
            SortedSet<SpellEntry> found = thirdPower.filterByExactLevel(i);
            counts.put(i, found);
            if (found != null) {
                presize += found.size();
            }
        }
        Truth.assertThat(spellbook.loadFromFile()).isTrue();
        int postSize = 0;
        for (Integer i = 0; i < 11; i++) {
            SortedSet<SpellEntry> found = thirdPower.filterByExactLevel(i);
            if (found != null) {
                postSize += found.size();
            }
            if (counts.keySet().contains(i)) {
                SortedSet<SpellEntry> seen = counts.get(i);
                if (found == null) {
                    Truth.assertThat(seen).isNull();
                    continue;
                }
                Truth.assertThat(found).isNotNull();
                if (seen != null) {
                    Truth.assertWithMessage("We musn't lose any spells").that(found).containsAtLeastElementsIn(seen)
                            .inOrder();
                }
            }
        }
        Truth.assertWithMessage("Spell list should only have grown!").that(postSize).isAtLeast(presize);
    }
}
