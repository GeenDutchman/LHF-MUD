package com.lhf.game.map.doors;

import com.lhf.game.map.Directions;
import com.lhf.game.map.Room;

public class StandardDoorway extends OneWayDoor {

    public StandardDoorway(Room roomA, Directions toRoomB, Room roomB) {
        super(roomA, toRoomB, roomB);

        Directions toRoomA = toRoomB.opposite();

        assert this.getRoomB().addExit(toRoomA, this);
    }

}
