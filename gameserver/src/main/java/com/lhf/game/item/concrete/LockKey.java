package com.lhf.game.item.concrete;

import java.util.Objects;
import java.util.UUID;

import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.item.Takeable;

public class LockKey extends Takeable {
    // TODO: make this usable?

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
            if (attemtper == null || !attemtper.hasItem(LockKey.generateKeyName(this.getLockUUID()))) {
                return false;
            }
            return true;
        }

        public default boolean canAccess(InventoryOwner attempter) {
            return this.isUnlocked() || this.isAuthorized(attempter);
        }
    }

    private final UUID lockedItemUuid;
    private final UUID keyUuid;

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