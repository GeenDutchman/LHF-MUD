package com.lhf.game.magic;

import java.io.IOException;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;

public class SpellbookTest {
    @Test
    void testSaving() throws IOException {
        Spellbook spellbook = new Spellbook();
        Truth.assertThat(spellbook.saveToFile()).isTrue();
    }

    @Test
    void testLoading() {
        Spellbook spellbook = new Spellbook();
        TreeMap<Integer, SortedSet<SpellEntry>> counts = new TreeMap<>();
        Integer presize = 0;
        for (Integer i = 0; i < 11; i++) {
            SortedSet<SpellEntry> found = spellbook.filterByExactLevel(i);
            counts.put(i, found);
            if (found != null) {
                presize += found.size();
            }
        }
        Truth.assertThat(spellbook.loadFromFile()).isTrue();
        int postSize = 0;
        for (Integer i = 0; i < 11; i++) {
            SortedSet<SpellEntry> found = spellbook.filterByExactLevel(i);
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

    @Test
    void testFilterByExactInvocation() {
        Spellbook spellbook = new Spellbook();
        NavigableSet<SpellEntry> optEntry = spellbook.filterByExactInvocation("bogux invocation");
        Truth.assertThat(optEntry.isEmpty()).isTrue();
        optEntry = spellbook.filterByExactInvocation("zarmamoo");
        Truth.assertThat(optEntry.isEmpty()).isFalse();
        Truth.assertThat(optEntry).hasSize(1);
        Truth.assertThat(optEntry.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByExactLevel() {
        Spellbook spellbook = new Spellbook();
        SortedSet<SpellEntry> found = spellbook.filterByExactLevel(-1);
        Truth.assertThat(found).hasSize(0);
        ThunderStrike thunderStrike = new ThunderStrike();
        found = spellbook.filterByExactLevel(0);
        Truth.assertThat(found.size()).isAtLeast(2);
        Truth.assertThat(found).doesNotContain(thunderStrike);
        found = spellbook.filterByExactLevel(1);
        Truth.assertThat(found.size()).isAtLeast(1);
        Truth.assertThat(found).contains(thunderStrike);
    }

    @Test
    void testFilterByExactName() {
        Spellbook spellbook = new Spellbook();
        NavigableSet<SpellEntry> found = spellbook.filterByExactName("fakexname");
        Truth.assertThat(found.isEmpty()).isTrue();
        found = spellbook.filterByExactName("Thaumaturgy");
        Truth.assertThat(found.isEmpty()).isFalse();
        Truth.assertThat(found).hasSize(1);
        Truth.assertThat(found.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByVocationName() {
        Spellbook spellbook = new Spellbook();
        SortedSet<SpellEntry> found = spellbook.filterByVocationName(null);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationName(VocationName.FIGHTER);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationName(VocationName.MAGE);
        Truth.assertThat(found.size()).isAtLeast(1);
        Truth.assertThat(found).contains(new ThunderStrike());
        int previous = 0;
        for (SpellEntry entry : found) {
            System.out.println(entry.toString());
            Truth.assertThat(entry.getLevel()).isAtLeast(previous);
            previous = entry.getLevel();
        }
    }

    @Test
    void testFilterByVocationAndLevels() {
        Spellbook spellbook = new Spellbook();
        SortedSet<SpellEntry> found = spellbook.filterByVocationAndLevels(null, null);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationAndLevels(VocationName.MAGE, Arrays.asList(1));
        Truth.assertThat(found.size()).isAtLeast(1);
    }
}
