package com.lhf.game.map.doors;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Directions;
import com.lhf.game.map.Room;

public class HiddenDoor extends Doorway {
    private Room hiddenRoom;
    private Room openRoom;

    public HiddenDoor(Room hiddenRoom, Directions toOpenRoom, Room openRoom) {
        this.hiddenRoom = hiddenRoom;
        this.openRoom = openRoom;

        assert this.hiddenRoom.addExit(toOpenRoom, this);
    }

    @Override
    public Room getRoomA() {
        return this.hiddenRoom;
    }

    @Override
    public Room getRoomB() {
        return this.openRoom;
    }

    @Override
    public boolean traverse(Creature creature) {
        if (this.openRoom.containsCreature(creature)) {
            return false;
        }
        return super.traverse(creature);
    }

}
