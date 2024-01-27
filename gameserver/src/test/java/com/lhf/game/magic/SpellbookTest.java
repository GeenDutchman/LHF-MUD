package com.lhf.game.magic;

import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.concrete.ThunderStrike;
import com.lhf.game.serialization.GsonBuilderFactory;

public class SpellbookTest {

    // private final static ExclusionStrategy noIDs = new ExclusionStrategy() {
    // @Override
    // public boolean shouldSkipClass(Class<?> clazz) {
    // return CreatureBuilderID.class.equals(clazz);
    // }

    // @Override
    // public boolean shouldSkipField(FieldAttributes f) {
    // return CreatureBuilderID.class.equals(f.getDeclaredClass());
    // }
    // };

    private static GsonBuilderFactory getGsonBuilderFactory() {
        return GsonBuilderFactory.start().spells().prettyPrinting();
        // .inlineRawBuilderAdjustment((builder) ->
        // builder.addSerializationExclusionStrategy(noIDs));
    }

    @Test
    void testSaving() throws IOException {
        Spellbook spellbook = new Spellbook();
        Truth.assertThat(spellbook.saveToFile(SpellbookTest.getGsonBuilderFactory())).isTrue();
    }

    @Test
    void testLoading() {
        Spellbook spellbook = new Spellbook();
        EnumMap<ResourceCost, SortedSet<SpellEntry>> counts = new EnumMap<>(ResourceCost.class);
        Integer presize = 0;
        for (ResourceCost i : ResourceCost.values()) {
            SortedSet<SpellEntry> found = spellbook.filterByExactLevel(i);
            counts.put(i, found);
            if (found != null) {
                presize += found.size();
            }
        }
        Truth.assertThat(spellbook.loadFromFile(SpellbookTest.getGsonBuilderFactory())).isTrue();
        int postSize = 0;
        for (ResourceCost i : ResourceCost.values()) {
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
        Spellbook spellbook = new Spellbook().addConcreteSpells();
        NavigableSet<SpellEntry> optEntry = spellbook.filterByExactInvocation("bogux invocation");
        Truth.assertThat(optEntry.isEmpty()).isTrue();
        optEntry = spellbook.filterByExactInvocation("zarmamoo");
        Truth.assertThat(optEntry.isEmpty()).isFalse();
        Truth.assertThat(optEntry).hasSize(1);
        Truth.assertThat(optEntry.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByExactLevel() {
        Spellbook spellbook = new Spellbook().addConcreteSpells();
        ThunderStrike thunderStrike = new ThunderStrike();
        SortedSet<SpellEntry> found = spellbook.filterByExactLevel(ResourceCost.NO_COST);
        Truth.assertThat(found.size()).isAtLeast(2);
        Truth.assertThat(found).doesNotContain(thunderStrike);
        found = spellbook.filterByExactLevel(ResourceCost.FIRST_MAGNITUDE);
        Truth.assertThat(found.size()).isAtLeast(1);
        Truth.assertThat(found).contains(thunderStrike);
    }

    @Test
    void testFilterByExactName() {
        Spellbook spellbook = new Spellbook().addConcreteSpells();
        NavigableSet<SpellEntry> found = spellbook.filterByExactName("fakexname");
        Truth.assertThat(found.isEmpty()).isTrue();
        found = spellbook.filterByExactName("Thaumaturgy");
        Truth.assertThat(found.isEmpty()).isFalse();
        Truth.assertThat(found).hasSize(1);
        Truth.assertThat(found.first()).isEqualTo(new Thaumaturgy());
    }

    @Test
    void testFilterByVocationName() {
        Spellbook spellbook = new Spellbook().addConcreteSpells();
        SortedSet<SpellEntry> found = spellbook.filterByVocationName(null);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationName(VocationName.FIGHTER);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationName(VocationName.MAGE);
        Truth.assertThat(found.size()).isAtLeast(1);
        Truth.assertThat(found).contains(new ThunderStrike());
        ResourceCost previous = ResourceCost.NO_COST;
        for (SpellEntry entry : found) {
            System.out.println(entry.toString());
            Truth.assertThat(entry.getLevel()).isAtLeast(previous);
            previous = entry.getLevel();
        }
    }

    @Test
    void testFilterByVocationAndLevels() {
        Spellbook spellbook = new Spellbook().addConcreteSpells();
        SortedSet<SpellEntry> found = spellbook.filterByVocationAndLevels(null, null);
        Truth.assertThat(found).hasSize(0);
        found = spellbook.filterByVocationAndLevels(VocationName.MAGE, EnumSet.of(ResourceCost.FIRST_MAGNITUDE));
        Truth.assertThat(found.size()).isAtLeast(1);
    }
}
