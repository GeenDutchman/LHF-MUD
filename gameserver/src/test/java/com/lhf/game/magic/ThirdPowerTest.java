package com.lhf.game.magic;

import java.util.Arrays;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;

public class ThirdPowerTest {
    @Test
    void testFilterByExactInvocation() {
        ThirdPower thirdPower = new ThirdPower(null, new Spellbook());
        NavigableSet<SpellEntry> optEntry = thirdPower.filterByExactInvocation("bogux invocation");
        Truth.assertThat(optEntry.isEmpty()).isTrue();
        optEntry = thirdPower.filterByExactInvocation("zarmamoo");
        Truth.assertThat(optEntry.isEmpty()).isFalse();
        Truth.assertThat(optEntry).hasSize(1);
        Truth.assertThat(optEntry.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByExactLevel() {
        ThirdPower thirdPower = new ThirdPower(null, new Spellbook());
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
        ThirdPower thirdPower = new ThirdPower(null, new Spellbook());
        NavigableSet<SpellEntry> found = thirdPower.filterByExactName("fakexname");
        Truth.assertThat(found.isEmpty()).isTrue();
        found = thirdPower.filterByExactName("Thaumaturgy");
        Truth.assertThat(found.isEmpty()).isFalse();
        Truth.assertThat(found).hasSize(1);
        Truth.assertThat(found.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByVocationName() {
        ThirdPower thirdPower = new ThirdPower(null, new Spellbook());
        SortedSet<SpellEntry> found = thirdPower.filterByVocationName(null);
        Truth.assertThat(found).hasSize(0);
        found = thirdPower.filterByVocationName(VocationName.FIGHTER);
        Truth.assertThat(found).hasSize(0);
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
        ThirdPower thirdPower = new ThirdPower(null, new Spellbook());
        SortedSet<SpellEntry> found = thirdPower.filterByVocationAndLevels(null, null);
        Truth.assertThat(found).hasSize(0);
        found = thirdPower.filterByVocationAndLevels(VocationName.MAGE, Arrays.asList(1));
        Truth.assertThat(found.size()).isAtLeast(1);
    }

}
