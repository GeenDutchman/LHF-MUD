package com.lhf.game.map;

import java.util.UUID;

import com.lhf.game.creature.Creature;

class Doorway {
    private UUID roomAUuid;
    private UUID roomBUuid;

    public Doorway(UUID roomAUuid, UUID roomBUuid) {
        this.roomAUuid = roomAUuid;
        this.roomBUuid = roomBUuid;
    }

    public UUID getRoomAUuid() {
        return roomAUuid;
    }

    public UUID getRoomBUuid() {
        return roomBUuid;
    }

    public boolean canTraverse(Creature creature) {
        return true;
    }

}
