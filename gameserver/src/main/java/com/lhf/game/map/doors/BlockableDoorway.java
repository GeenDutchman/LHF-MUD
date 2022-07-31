package com.lhf.game.map.doors;

import com.lhf.game.creature.Creature;

public interface BlockableDoorway extends Doorway {

    public void open();

    public void close();

    public boolean isOpen();

    @Override
    public default boolean traverse(Creature creature) {
        if (!this.isOpen()) {
            return false;
        }
        return Doorway.super.traverse(creature);
    }

}
