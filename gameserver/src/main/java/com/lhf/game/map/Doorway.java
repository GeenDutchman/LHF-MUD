package com.lhf.game.map;

import com.lhf.game.creature.ICreature;

public class Doorway implements Comparable<Doorway> {
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

    @Override
    public String toString() {
        return className;
    }

    @Override
    public int compareTo(Doorway arg0) {
        if (arg0 == null) {
            throw new NullPointerException();
        }
        if (this == arg0) {
            return 0;
        }
        return this.getClassName().compareTo(arg0.getClassName());
    }

}
