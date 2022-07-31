package com.lhf.game.map.doors;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Room;

public class OneWayDoor extends StandardDoorway {

    public OneWayDoor(Room hiddenRoom, Room openRoom) {
        super(hiddenRoom, openRoom);
    }

    @Override
    public boolean traverse(Creature creature) {
        if (this.getRoomB().containsCreature(creature)) {
            return false;
        }
        return super.traverse(creature);
    }

}
