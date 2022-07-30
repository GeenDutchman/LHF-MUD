package com.lhf.game.map;

import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.Creature;

public abstract class BlockableDoorway extends Doorway {
    private AtomicBoolean opened = new AtomicBoolean(false);

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
    public boolean traverse(Creature creature) {
        if (!this.isOpen()) {
            return false;
        }
        return super.traverse(creature);
    }

}
