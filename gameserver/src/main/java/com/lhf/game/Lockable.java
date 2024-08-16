package com.lhf.game;

import java.util.Optional;
import java.util.UUID;

import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.LockKey;

public interface Lockable {
    public default IItem generateKey() {
        if (this.isUnlocked()) {
            this.lock();
        }
        IItem.IItemBuilder builder = this.generateKeyBuilder();
        return builder != null ? builder.build() : null;
    }

    public default IItem.IItemBuilder generateKeyBuilder() {
        if (this.isUnlocked()) {
            this.lock();
        }
        IItem.IItemBuilder builder = new Item.ItemBuilder().setName(this.generateKeyName()).setTakeable(true)
                .setVisible(true).setDescriptionString("A key for ... something.")
                .adjustUsableCapability(usable -> usable.setTotalNumberUsableTimes(1));
        return builder;
    }

    public UUID getLockUUID();

    public boolean isUnlocked();

    public void unlock();

    public void lock();

    public default String generateKeyName() {
        return "Key " + this.getLockUUID().toString();
    }

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