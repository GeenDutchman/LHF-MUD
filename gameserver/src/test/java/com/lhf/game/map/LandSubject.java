package com.lhf.game.map;

import java.util.UUID;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Truth;
import com.lhf.game.AtlasSubject;
import com.lhf.game.CreatureContainerSubject;
import com.lhf.game.creature.ICreature;

public class LandSubject extends CreatureContainerSubject {
    public static Factory<LandSubject, Land> lands() {
        return LandSubject::new;
    }

    public static LandSubject assertThat(Land actual) {
        return Truth.assertAbout(lands()).that(actual);
    }

    private final Land actual;

    protected LandSubject(FailureMetadata metadata, Land actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public AtlasSubject<Area, UUID, Directions, Doorway> atlas() {
        return check("getAtlas()").about(AtlasSubject.atlases()).that(actual.getAtlas());
    }

    public ComparableSubject<UUID> startingAreaUUID() {
        return check("getStartingAreaUUID()").that(actual.getStartingAreaUUID());
    }

    public AreaSubject startingArea() {
        return check("getStartingArea()").about(AreaSubject.areas()).that(actual.getStartingArea());
    }

    public IterableSubject areaExits(Area area) {
        return check("getAreaExits(%s)", area).that(actual.getAreaExits(area));
    }

    public OptionalSubject areaByName(String name) {
        return check("getAreaByName(%s)", name).that(actual.getAreaByName(name));
    }

    public AreaSubject areaForCreature(ICreature creature) {
        return check("getCreatureArea(%s)", creature).about(AreaSubject.areas()).that(actual.getCreatureArea(creature));
    }

    public AreaSubject areaForNamedCreature(String name) {
        return check("getCreatureArea(%s)", name).about(AreaSubject.areas()).that(actual.getCreatureArea(name));
    }
}
