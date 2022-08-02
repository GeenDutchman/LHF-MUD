package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.Creature;

class OneWayDoorway extends Doorway {

    public OneWayDoorway(UUID roomAUuid, Directions fromBtoA, UUID roomBUuid) {
        super(roomAUuid, fromBtoA, roomBUuid);
    }

    public boolean canTraverse(Creature creature, Directions whichWay) {
        if (whichWay != this.getFromBtoA()) {
            return false;
        }
        return super.canTraverse(creature, whichWay);
    }

}
