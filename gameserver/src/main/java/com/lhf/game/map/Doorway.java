package com.lhf.game.map;

import com.lhf.game.creature.ICreature;

public class Doorway {
    protected final String className;

    public Doorway() {
        this.className = this.getClass().getName();
    }

    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        return true;
    }

    public String getClassName() {
        return className;
    }

}
