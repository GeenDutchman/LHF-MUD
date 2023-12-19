package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.DoorwayFactory.DoorwayType;

class KeyedDoorway extends CloseableDoorway implements Lockable {
    private UUID doorwayUuid;

    public KeyedDoorway(UUID roomAUuid, Directions fromBtoA, UUID roomBUuid) {
        super(roomAUuid, fromBtoA, roomBUuid);
        this.close();
        this.doorwayUuid = UUID.randomUUID();
    }

    @Override
    public DoorwayType getType() {
        return DoorwayType.KEYED;
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
    public boolean canTraverse(ICreature creature, Directions whichWay) {
        if (!this.canAccess(creature)) {
            return false;
        }
        if (this.accessUnlocks()) {
            this.open();
        }
        return true;
    }

}
