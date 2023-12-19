package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.ICreature;
import com.lhf.game.map.DoorwayFactory.DoorwayType;

class OneWayDoorway extends Doorway {

    public OneWayDoorway(UUID roomAUuid, Directions fromBtoA, UUID roomBUuid) {
        super(roomAUuid, fromBtoA, roomBUuid);
    }

    @Override
    public DoorwayType getType() {
        return DoorwayType.ONE_WAY;
    }

    public boolean canTraverse(ICreature creature, Directions whichWay) {
        if (whichWay != this.getFromBtoA()) {
            return false;
        }
        return super.canTraverse(creature, whichWay);
    }

}
