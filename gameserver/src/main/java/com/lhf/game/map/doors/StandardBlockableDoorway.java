package com.lhf.game.map.doors;

import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Room;

public class StandardBlockableDoorway extends StandardDoorway implements BlockableDoorway {
    private AtomicBoolean opened = new AtomicBoolean(false);

    public StandardBlockableDoorway(Room roomA, Room roomB) {
        super(roomA, roomB);
    }

    @Override
    public void open() {
        this.opened.set(true);
    }

    @Override
    public void close() {
        this.opened.set(false);
    }

    @Override
    public boolean isOpen() {
        return this.opened.get();
    }

    @Override
    public boolean traverse(Creature creature) {
        return BlockableDoorway.super.traverse(creature);
    }

}
