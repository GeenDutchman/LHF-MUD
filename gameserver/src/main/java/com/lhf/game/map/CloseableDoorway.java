package com.lhf.game.map;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.Creature;

class CloseableDoorway extends Doorway {
    private AtomicBoolean opened;

    public CloseableDoorway(UUID roomAUuid, UUID roomBUuid) {
        super(roomAUuid, roomBUuid);
        this.opened = new AtomicBoolean(false);
    }

    public CloseableDoorway(UUID roomAUuid, UUID roomBUuid, boolean opened) {
        super(roomAUuid, roomBUuid);
        this.opened = new AtomicBoolean(opened);
    }

    public void open() {
        this.opened.set(true);
    }

    public void close() {
        this.opened.set(false);
    }

    public boolean isOpen() {
        return this.opened.get();
    }

    @Override
    public boolean canTraverse(Creature creature) {
        if (this.isOpen()) {
            return super.canTraverse(creature);
        }
        return false;
    }

}
