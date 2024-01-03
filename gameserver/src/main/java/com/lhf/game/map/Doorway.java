package com.lhf.game.map;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Land.TraversalTester;

class Doorway implements TraversalTester {

    public enum DoorwayType {
        STANDARD, ONE_WAY, CLOSEABLE, KEYED;
    }

    public Doorway() {

    }

    public DoorwayType getType() {
        return DoorwayType.STANDARD;
    }

    @Override
    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        return true;
    }

}
