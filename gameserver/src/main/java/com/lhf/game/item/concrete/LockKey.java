package com.lhf.game.item.concrete;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.item.Item;
import com.lhf.game.item.Usable;

public class LockKey extends Usable {

    public interface Lockable {
        public default LockKey generateKey() {
            this.lock();
            return new LockKey(this.getLockUUID());
        }

        public UUID getLockUUID();

        public boolean isUnlocked();

        public void unlock();

        public void lock();

        public default boolean isAuthorized(InventoryOwner attemtper) {
            String keyName = LockKey.generateKeyName(this.getLockUUID());
            Optional<Item> retrieved = attemtper.getItem(keyName);
            if (attemtper == null || retrieved.isEmpty()) {
                return false;
            }
            if (retrieved.get() instanceof LockKey retrievedKey) {
                if (!retrievedKey.hasUsesLeft()) {
                    return false;
                }
                if (!retrievedKey.useOnce()) {
                    attemtper.removeItem(retrievedKey);
                }
                return true;
            }
            return false;
        }

        public default boolean canAccess(InventoryOwner attempter) {
            return this.isUnlocked() || this.isAuthorized(attempter);
        }

        public default boolean accessUnlocks() {
            return true;
        }
    }

    private final UUID lockedItemUuid;
    private final UUID keyUuid;

    /**
     * Creates a LockKey keyed to an item's UUID. Only usable once.
     * 
     * @param lockedItemUuid
     */
    public LockKey(UUID lockedItemUuid) {
        super(LockKey.generateKeyName(lockedItemUuid), true, 1);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
        this.descriptionString = "A key for ... something.";
    }

    /**
     * Creates a LockKey keyed to an item's UUID.
     * 
     * @param lockedItemUuid
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public LockKey(UUID lockedItemUuid, int useSoManyTimes) {
        super(LockKey.generateKeyName(lockedItemUuid), true, useSoManyTimes);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
        this.descriptionString = "A key for ... something.";
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