package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.ICreature;

public class Doorway implements Comparable<Doorway> {
    protected final String className;
    private final UUID uuid = UUID.randomUUID();

    public Doorway() {
        this.className = this.getClass().getName();
    }

    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        return true;
    }

    public String getClassName() {
        return className;
    }

    protected UUID getUuid() {
        return uuid;
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
        int comparison = this.getClassName().compareTo(arg0.getClassName());
        if (comparison != 0) {
            return comparison;
        }
        return this.uuid.compareTo(arg0.uuid);
    }

}
