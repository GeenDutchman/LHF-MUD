package com.lhf.game.map;

import java.util.UUID;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Truth;
import com.lhf.game.CreatureContainerSubject;
import com.lhf.game.creature.ICreature;

public class AreaSubject extends ComparableSubject<Area> {
    public static Factory<AreaSubject, Area> areas() {
        return AreaSubject::new;
    }

    public static AreaSubject assertThat(Area actual) {
        return Truth.assertAbout(areas()).that(actual);
    }

    private final Area actual;

    protected AreaSubject(FailureMetadata metadata, Area actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public ComparableSubject<UUID> uuid() {
        return check("getUuid()").that(actual.getUuid());
    }

    public CreatureContainerSubject asCreatureContainer() {
        return check("this").about(CreatureContainerSubject.creatureContainers()).that(actual);
    }

    public LandSubject land() {
        return check("getLand()").about(LandSubject.lands()).that(actual.getLand());
    }

    public void creatureIsRemoved(ICreature c, Directions dir) {
        check("removeCreature(%s, %s)", c, dir).that(actual.removeCreature(c, dir)).isTrue();
    }

    public IterableSubject subAreas() {
        return check("getSubAreas()").that(actual.getSubAreas());
    }

}
