package com.lhf.game.map.doors;

import com.lhf.game.map.Directions;
import com.lhf.game.map.Room;

public class StandardBlockableDoorway extends BlockableDoorway {
    private Room roomA;
    private Room roomB;

    public StandardBlockableDoorway(Room roomA, Directions toRoomB, Room roomB) {
        this.roomA = roomA;
        this.roomB = roomB;

        Directions toRoomA = toRoomB.opposite();

        assert this.roomA.addExit(toRoomB, this);
        assert this.roomB.addExit(toRoomA, this);
    }

    @Override
    public Room getRoomA() {
        return this.roomA;
    }

    @Override
    public Room getRoomB() {
        return this.roomB;
    }

}
