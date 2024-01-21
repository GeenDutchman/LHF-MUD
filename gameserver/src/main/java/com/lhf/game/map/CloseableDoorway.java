package com.lhf.game.map;

import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.ICreature;

public class CloseableDoorway extends Doorway {
    private AtomicBoolean opened;

    public CloseableDoorway() {
        this.opened = new AtomicBoolean(false);
    }

    public CloseableDoorway(boolean opened) {
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
    public boolean testTraversal(ICreature creature, Directions direction, Area source, Area dest) {
        if (this.isOpen()) {
            return super.testTraversal(creature, direction, source, dest);
        }
        return false;
    }

}
