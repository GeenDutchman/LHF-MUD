package com.lhf.game.item.concrete;

import java.util.Objects;
import java.util.UUID;

import com.lhf.game.item.ItemVisitor;
import com.lhf.game.item.Usable;

public class LockKey extends Usable {

    private final UUID lockedItemUuid;
    private final UUID keyUuid;

    /**
     * Creates a LockKey keyed to an item's UUID. Only usable once.
     * 
     * @param lockedItemUuid
     */
    public LockKey(UUID lockedItemUuid) {
        super(LockKey.generateKeyName(lockedItemUuid), "A key for ... something.", 1, null, null);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    /**
     * Creates a LockKey keyed to an item's UUID.
     * 
     * @param lockedItemUuid
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public LockKey(UUID lockedItemUuid, int useSoManyTimes) {
        super(LockKey.generateKeyName(lockedItemUuid), "A key for ... something.", useSoManyTimes, null, null);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    public LockKey(UUID lockedItemUuid, int useSoManyTimes, ItemVisitor itemVisitor) {
        super(LockKey.generateKeyName(lockedItemUuid), "A key for ... something.", useSoManyTimes, null, itemVisitor);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    @Override
    public LockKey makeCopy() {
        return new LockKey(lockedItemUuid, this.numCanUseTimes);
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