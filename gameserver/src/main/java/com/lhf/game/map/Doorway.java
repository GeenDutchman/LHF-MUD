package com.lhf.game.map;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Land.TraversalTester;

class Doorway implements TraversalTester {
    protected final String className;

    public enum DoorwayType {
        STANDARD, ONE_WAY, CLOSEABLE, KEYED;
    }

    public Doorway() {
        this.className = this.getClass().getName();
    }

    public DoorwayType getType() {
        return DoorwayType.STANDARD;
    }

    @Override
    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        return true;
    }

    public String getClassName() {
        return className;
    }

}
