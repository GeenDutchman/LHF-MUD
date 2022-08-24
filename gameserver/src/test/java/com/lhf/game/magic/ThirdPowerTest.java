package com.lhf.game.magic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;

public class ThirdPowerTest {
    @Test
    void testFilterByExactInvocation() {
        ThirdPower thirdPower = new ThirdPower(null);
        Optional<SpellEntry> optEntry = thirdPower.filterByExactInvocation("bogux invocation");
        Truth.assertThat(optEntry.isEmpty()).isTrue();
        optEntry = thirdPower.filterByExactInvocation("zarmamoo");
        Truth.assertThat(optEntry.isPresent()).isTrue();
        Truth.assertThat(optEntry.get()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByExactLevel() {
        ThirdPower thirdPower = new ThirdPower(null);
        SortedSet<SpellEntry> found = thirdPower.filterByExactLevel(-1);
        Truth.assertThat(found).hasSize(0);
        ThunderStrike thunderStrike = new ThunderStrike();
        found = thirdPower.filterByExactLevel(0);
        Truth.assertThat(found.size()).isAtLeast(2);
        Truth.assertThat(found).doesNotContain(thunderStrike);
        found = thirdPower.filterByExactLevel(1);
        Truth.assertThat(found.size()).isAtLeast(1);
        Truth.assertThat(found).contains(thunderStrike);
    }

    @Test
    void testFilterByExactName() {
        ThirdPower thirdPower = new ThirdPower(null);
        Optional<SpellEntry> found = thirdPower.filterByExactName("fakexname");
        Truth.assertThat(found.isEmpty()).isTrue();
        found = thirdPower.filterByExactName("Thaumaturgy");
        Truth.assertThat(found.isPresent()).isTrue();
        Truth.assertThat(found.get()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByVocationName() {
        ThirdPower thirdPower = new ThirdPower(null);
        SortedSet<SpellEntry> found = thirdPower.filterByVocationName(null);
        Truth.assertThat(found).hasSize(2);
        found = thirdPower.filterByVocationName(VocationName.FIGHTER);
        Truth.assertThat(found.size()).isAtLeast(1);
        found = thirdPower.filterByVocationName(VocationName.MAGE);
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
        ThirdPower thirdPower = new ThirdPower(null);
        SortedSet<SpellEntry> found = thirdPower.filterByVocationAndLevels(null, null);
        Truth.assertThat(found).hasSize(0);
        found = thirdPower.filterByVocationAndLevels(VocationName.MAGE, Arrays.asList(1));
        Truth.assertThat(found.size()).isAtLeast(1);
    }

    @Test
    void testSaving() throws IOException {
        ThirdPower thirdPower = new ThirdPower(null);
        Truth.assertThat(thirdPower.saveToFile()).isTrue();
    }

    @Test
    void testLoading() {
        ThirdPower thirdPower = new ThirdPower(null);
        TreeMap<Integer, SortedSet<SpellEntry>> counts = new TreeMap<>();
        Integer presize = 0;
        for (Integer i = 0; i < 11; i++) {
            SortedSet<SpellEntry> found = thirdPower.filterByExactLevel(i);
            counts.put(i, found);
            if (found != null) {
                presize += found.size();
            }
        }
        Truth.assertThat(thirdPower.loadFromFile()).isTrue();
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
