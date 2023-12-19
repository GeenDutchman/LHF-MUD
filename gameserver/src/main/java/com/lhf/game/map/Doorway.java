package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.DoorwayFactory.DoorwayType;

class Doorway {
    private UUID roomAUuid;
    private Directions fromBtoA;
    private UUID roomBUuid;

    public Doorway(UUID roomAUuid, Directions fromBtoA, UUID roomBUuid) {
        this.roomAUuid = roomAUuid;
        this.fromBtoA = fromBtoA;
        this.roomBUuid = roomBUuid;
    }

    public DoorwayType getType() {
        return DoorwayType.STANDARD;
    }

    public UUID getRoomAUuid() {
        return roomAUuid;
    }

    public UUID getRoomBUuid() {
        return roomBUuid;
    }

    public Directions getFromBtoA() {
        return fromBtoA;
    }

    public UUID getRoomAccross(UUID presentUuid) {
        if (this.getRoomAUuid().equals(presentUuid)) {
            return this.getRoomBUuid();
        } else if (this.getRoomBUuid().equals(presentUuid)) {
            return this.getRoomAUuid();
        }
        return null;
    }

    public boolean canTraverse(ICreature creature, Directions whichWay) {
        return true;
    }

}
