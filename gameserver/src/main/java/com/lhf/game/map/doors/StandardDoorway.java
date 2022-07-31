package com.lhf.game.map.doors;

import com.lhf.game.map.Room;

public class StandardDoorway implements Doorway {
    private Room roomA;
    private Room roomB;

    public StandardDoorway(Room roomA, Room roomB) {
        this.roomA = roomA;
        this.roomB = roomB;
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
