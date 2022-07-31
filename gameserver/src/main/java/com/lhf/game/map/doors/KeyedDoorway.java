package com.lhf.game.map.doors;

import java.util.UUID;

import com.lhf.game.creature.Creature;

public abstract class KeyedDoorway extends BlockableDoorway {

    private UUID doorwayUuid = UUID.randomUUID();

    public UUID getUuid() {
        return this.doorwayUuid;
    }

    public boolean canTraverse(Creature creature) {
        String keyname = DoorKey.generateKeyName(this.doorwayUuid);
        if (creature.hasItem(keyname)) {
            this.open();
            creature.removeItem(keyname);
        }
        return this.isOpen();
    }

    @Override
    public boolean traverse(Creature creature) {
        if (!this.canTraverse(creature)) {
            return false;
        }
        return super.traverse(creature);
    }

    public DoorKey generateKey() {
        return new DoorKey(this.getUuid());
    }

}
