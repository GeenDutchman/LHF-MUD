package com.lhf.game.map.doors;

import java.util.UUID;

import com.lhf.game.creature.Creature;

public interface KeyedDoorway extends BlockableDoorway {

    public UUID getUuid();

    public default boolean canTraverse(Creature creature) {
        String keyname = DoorKey.generateKeyName(this.getUuid());
        if (creature.hasItem(keyname)) {
            this.open();
            creature.removeItem(keyname);
        }
        return this.isOpen();
    }

    @Override
    public default boolean traverse(Creature creature) {
        if (!this.canTraverse(creature)) {
            return false;
        }
        return BlockableDoorway.super.traverse(creature);
    }

    public default DoorKey generateKey() {
        return new DoorKey(this.getUuid());
    }

}
