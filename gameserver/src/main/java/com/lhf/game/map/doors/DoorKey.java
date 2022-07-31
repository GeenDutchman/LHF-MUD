package com.lhf.game.map.doors;

import java.util.Objects;
import java.util.UUID;

import com.lhf.game.item.Item;

public class DoorKey extends Item {
    private UUID doorwayUuid;
    private UUID keyUuid;

    DoorKey(UUID doorwayUuid) {
        super(DoorKey.generateKeyName(doorwayUuid), true, "A key for ... something.");
        this.doorwayUuid = doorwayUuid;
        this.keyUuid = UUID.randomUUID();
    }

    static String generateKeyName(UUID doorwayUuid) {
        return "Key " + doorwayUuid.toString();
    }

    public UUID getDoorwayUuid() {
        return doorwayUuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(doorwayUuid, keyUuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof DoorKey)) {
            return false;
        }
        DoorKey other = (DoorKey) obj;
        return Objects.equals(doorwayUuid, other.doorwayUuid) && Objects.equals(keyUuid, other.keyUuid);
    }

}