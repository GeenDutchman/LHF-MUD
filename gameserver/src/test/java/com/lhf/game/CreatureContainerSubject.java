package com.lhf.game;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.creature.ICreature;

public class CreatureContainerSubject extends Subject {
    public static Factory<CreatureContainerSubject, CreatureContainer> creatureContainers() {
        return CreatureContainerSubject::new;
    }

    public static CreatureContainerSubject assertThat(CreatureContainer actual) {
        return Truth.assertAbout(creatureContainers()).that(actual);
    }

    private final CreatureContainer actual;

    protected CreatureContainerSubject(FailureMetadata metadata, CreatureContainer actual) {
        super(metadata, actual);
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

    public IterableSubject filteredCreatures(CreatureFilterQuery query) {
        return check("filterCreatures(%s)", query).that(actual.filterCreatures(query));
    }

    public void hasCreatureByName(String name) {
        check("hasCreature(%s)", name).that(actual.hasCreature(name)).isTrue();
    }

    public void hasCreature(ICreature c) {
        this.creatures().contains(c);
    }
}
