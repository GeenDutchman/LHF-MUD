package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.Creature;

class Doorway {
    private UUID roomAUuid;
    private Directions fromBtoA;
    private UUID roomBUuid;

    public Doorway(UUID roomAUuid, Directions fromBtoA, UUID roomBUuid) {
        this.roomAUuid = roomAUuid;
        this.fromBtoA = fromBtoA;
        this.roomBUuid = roomBUuid;
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

    public boolean canTraverse(Creature creature, Directions whichWay) {
        return true;
    }

}
