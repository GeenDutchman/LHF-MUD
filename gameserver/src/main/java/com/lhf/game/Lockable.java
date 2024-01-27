package com.lhf.game;

import java.util.Optional;
import java.util.UUID;

import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.item.IItem;
import com.lhf.game.item.concrete.LockKey;

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
        Optional<IItem> retrieved = attemtper.getItem(keyName);
        if (attemtper == null || retrieved.isEmpty()) {
            return false;
        }
        if (retrieved.get() instanceof LockKey retrievedKey) {
            if (!retrievedKey.hasUsesLeft()) {
                attemtper.removeItem(retrievedKey);
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