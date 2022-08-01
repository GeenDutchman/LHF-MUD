package com.lhf.game.map.doors;

import java.util.UUID;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Room;

public class StandardKeyedDoorway extends StandardBlockableDoorway implements KeyedDoorway {
    private UUID doorwayUuid;

    public StandardKeyedDoorway(Room roomA, Room roomB) {
        super(roomA, roomB);
        this.doorwayUuid = UUID.randomUUID();
    }

    @Override
    public UUID getUuid() {
        return this.doorwayUuid;
    }

    @Override
    public boolean traverse(Creature creature) {
        return KeyedDoorway.super.traverse(creature);
    }

}
