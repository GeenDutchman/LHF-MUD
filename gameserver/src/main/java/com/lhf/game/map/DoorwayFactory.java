package com.lhf.game.map;

import java.util.UUID;

public class DoorwayFactory {
    public enum DoorwayType {
        STANDARD, ONE_WAY, CLOSEABLE, KEYED;
    }

    public static Doorway createDoorway(DoorwayType type, UUID toAdd, Directions toExistingRoom, UUID existing) {
        if (type == null) {
            return new Doorway(existing, toExistingRoom, toAdd);
        }
        switch (type) {
            case ONE_WAY:
                return new OneWayDoorway(existing, toExistingRoom, toAdd);
            case CLOSEABLE:
                return new CloseableDoorway(existing, toExistingRoom, toAdd);
            case KEYED:
                return new KeyedDoorway(existing, toExistingRoom, toAdd);
            case STANDARD:
            default:
                return new Doorway(existing, toExistingRoom, toAdd);
        }
    }
}
