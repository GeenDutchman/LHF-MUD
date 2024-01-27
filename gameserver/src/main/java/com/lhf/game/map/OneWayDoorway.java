package com.lhf.game.map;

import com.lhf.game.creature.ICreature;

public class OneWayDoorway extends Doorway {
    private final Directions allowed;

    public OneWayDoorway(Directions allowed) {
        this.allowed = allowed;
    }

    @Override
    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        if (this.allowed != null && !this.allowed.equals(direction)) {
            return false;
        }
        return super.testTraversal(creature, direction, source, dest);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OneWayDoorway [allowed=").append(allowed).append("]");
        return builder.toString();
    }

}
