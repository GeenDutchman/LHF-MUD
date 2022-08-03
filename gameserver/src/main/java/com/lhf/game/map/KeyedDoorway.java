package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.concrete.LockKey;
import com.lhf.game.map.DoorwayFactory.DoorwayType;

class KeyedDoorway extends CloseableDoorway {
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

    public LockKey generateKey() {
        return new LockKey(this.getDoorwayUuid());
    }

    public UUID getDoorwayUuid() {
        return doorwayUuid;
    }

    @Override
    public boolean canTraverse(Creature creature, Directions whichWay) {
        String keyName = LockKey.generateKeyName(this.getDoorwayUuid());
        if (creature.hasItem(keyName)) {
            this.open();
            creature.removeItem(keyName);
        }
        return super.canTraverse(creature, whichWay);
    }

}
