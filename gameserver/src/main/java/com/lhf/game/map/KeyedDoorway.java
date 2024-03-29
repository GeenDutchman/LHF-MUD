package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;

public class KeyedDoorway extends CloseableDoorway implements Lockable {
    private final UUID doorwayUuid;

    public KeyedDoorway() {
        this.doorwayUuid = UUID.randomUUID();
    }

    public KeyedDoorway(boolean opened) {
        super(opened);
        this.doorwayUuid = UUID.randomUUID();
    }

    @Override
    public void lock() {
        this.close();
    }

    @Override
    public UUID getLockUUID() {
        return this.doorwayUuid;
    }

    @Override
    public boolean isUnlocked() {
        return this.isOpen();
    }

    @Override
    public void unlock() {
        this.open();
    }

    @Override
    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        if (!this.canAccess(creature)) {
            return false;
        }
        if (this.accessUnlocks()) {
            this.open();
        }
        return super.testTraversal(creature, direction, source, dest);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("KeyedDoorway [doorwayUuid=").append(doorwayUuid).append(", opened=").append(this.isOpen())
                .append("]");
        return builder.toString();
    }

}
