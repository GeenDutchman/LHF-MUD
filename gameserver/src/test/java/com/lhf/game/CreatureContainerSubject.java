package com.lhf.game;

import java.util.Objects;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Truth;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.creature.ICreature;

public class CreatureContainerSubject extends IterableSubject {
    public static Factory<CreatureContainerSubject, CreatureContainer> creatureContainers() {
        return CreatureContainerSubject::new;
    }

    public static CreatureContainerSubject assertThat(CreatureContainer actual) {
        return Truth.assertAbout(creatureContainers()).that(actual);
    }

    private final CreatureContainer actual;

    protected CreatureContainerSubject(FailureMetadata metadata, CreatureContainer actual) {
        super(metadata, actual != null ? actual.getCreatures() : null);
        this.actual = actual;
    }

    public IterableSubject creatures() {
        return check("getCreatures()").that(actual.getCreatures());
    }

    public void creatureIsAdded(ICreature c) {
        check("addCreature(%s)", c).that(actual.addCreature(c)).isTrue();
        this.creatures().contains(c);
    }

    public void creatureIsNotAdded(ICreature c) {
        check("addCreature(%s)", c).that(actual.addCreature(c)).isFalse();
        this.creatures().doesNotContain(c);
    }

    public void creatureIsRemoved(ICreature c) {
        check("removeCreature(%s)", c).that(actual.removeCreature(c)).isTrue();
        this.creatures().doesNotContain(c);
    }

    public IterableSubject filteredCreatures(CreatureFilterQuery query) {
        return check("filterCreatures(%s)", query).that(actual.filterCreatures(query));
    }

    public void hasCreatureByName(String name) {
        check("hasCreature(%s)", name).that(actual.hasCreature(name)).isTrue();
    }

    public void hasCreature(ICreature c) {
        this.creatures().contains(c);
    }

    public void doesNotHaveCreature(ICreature c) {
        this.creatures().doesNotContain(c);
    }

    @Override
    public void isEqualTo(Object expected) {
        @SuppressWarnings("UndefinedEquals") // method contract requires testing iterables for equality
        boolean equal = Objects.equals(actual, expected);
        if (equal) {
            return;
        }

        if (expected instanceof CreatureContainer expectedCC) {
            containsExactlyElementsIn(expectedCC.getCreatures());
        } else {
            super.isEqualTo(expected);
        }
    }
}
