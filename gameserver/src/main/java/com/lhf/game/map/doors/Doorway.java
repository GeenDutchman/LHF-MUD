package com.lhf.game.map.doors;

import com.lhf.game.creature.Creature;
import com.lhf.game.map.Room;

public abstract class Doorway {
    public abstract Room getRoomA();

    public abstract Room getRoomB();

    public boolean traverse(Creature creature) {

        Room roomA = this.getRoomA();
        Room roomB = this.getRoomB();

        if (roomA.containsCreature(creature)) {
            creature.setSuccessor(roomB);
            roomB.addCreature(creature);
            roomA.removeCreature(creature);
            return true;
        } else if (roomB.containsCreature(creature)) {
            creature.setSuccessor(roomA);
            roomA.addCreature(creature);
            roomB.removeCreature(creature);
            return true;
        }
        return false;
    }
}
