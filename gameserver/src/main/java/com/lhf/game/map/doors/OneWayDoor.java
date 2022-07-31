package com.lhf.game.map.doors;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Directions;
import com.lhf.game.map.Room;

public class OneWayDoor implements Doorway {
    private Room roomA;
    private Room roomB;

    public OneWayDoor(Room hiddenRoom, Directions toOpenRoom, Room openRoom) {
        this.roomA = hiddenRoom;
        this.roomB = openRoom;

        assert this.roomA.addExit(toOpenRoom, this);
    }

    @Override
    public Room getRoomA() {
        return this.roomA;
    }

    @Override
    public Room getRoomB() {
        return this.roomB;
    }

    @Override
    public boolean traverse(Creature creature) {
        if (this.roomB.containsCreature(creature)) {
            return false;
        }
        return Doorway.super.traverse(creature);
    }

}
