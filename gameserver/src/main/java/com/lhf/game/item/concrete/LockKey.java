package com.lhf.game.item.concrete;

import java.util.Objects;
import java.util.UUID;

import com.lhf.game.item.Item;

public class LockKey extends Item {
    private UUID lockedItemUuid;
    private UUID keyUuid;

    public LockKey(UUID lockedItemUuid) {
        super(LockKey.generateKeyName(lockedItemUuid), true, "A key for ... something.");
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    public static String generateKeyName(UUID lockedItemUuid) {
        return "Key " + lockedItemUuid.toString();
    }

    public UUID getDoorwayUuid() {
        return lockedItemUuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lockedItemUuid, keyUuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LockKey)) {
            return false;
        }
        LockKey other = (LockKey) obj;
        return Objects.equals(lockedItemUuid, other.lockedItemUuid) && Objects.equals(keyUuid, other.keyUuid);
    }

}