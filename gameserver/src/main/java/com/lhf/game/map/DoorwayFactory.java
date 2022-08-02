package com.lhf.game.map;

public class DoorwayFactory {
    public enum DoorwayType {
        STANDARD, ONE_WAY, CLOSEABLE, KEYED;
    }

    public static Doorway createDoorway(DoorwayType type, Room existing, Directions toExistingRoom, Room toAdd) {
        switch (type) {
            case ONE_WAY:
                return new OneWayDoorway(existing.getUuid(), toExistingRoom, toAdd.getUuid());
            case CLOSEABLE:
                return new CloseableDoorway(existing.getUuid(), toExistingRoom, toAdd.getUuid());
            case KEYED:
                return new KeyedDoorway(existing.getUuid(), toExistingRoom, toAdd.getUuid());
            case STANDARD:
            default:
                return new Doorway(existing.getUuid(), toExistingRoom, toAdd.getUuid());
        }
    }
}
