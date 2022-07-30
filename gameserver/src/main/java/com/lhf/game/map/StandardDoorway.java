package com.lhf.game.map;

public class StandardDoorway extends Doorway {
    private Room roomA;
    private Room roomB;

    public StandardDoorway(Room roomA, Directions toRoomB, Room roomB) {
        this.roomA = roomA;
        this.roomB = roomB;

        Directions toRoomA = toRoomB.opposite();

        this.roomA.addExit(toRoomB.toString(), roomB);
        this.roomB.addExit(toRoomA.toString(), roomA);
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