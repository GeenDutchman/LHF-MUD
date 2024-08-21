package com.lhf.game;

import java.util.Optional;
import java.util.UUID;

import com.lhf.game.creature.inventory.InventoryOwner;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemEffectSource;
import com.lhf.game.item.LockingCapability;
import com.lhf.game.item.UsableCapability;

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
        // TODO: does this need an ItemFilterQuery?
        IItem.IItemBuilder builder = new Item.ItemBuilder().setName(this.generateKeyName()).setTakeable(true)
                .setVisible(true).setDescriptionString("A key for ... something.")
                .adjustUsableCapability(usable -> usable.setTotalNumberUsableTimes(1)
                        .addUseOnItemEffect(ItemEffectSource.getItemEffectBuilder("Turn Key").instantPersistence()
                                .setLockingCapabilityDelta(LockingCapability.Delta.TOGGLE)));
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
        if (attemtper == null) {
            return false;
        }
        final String keyName = this.generateKeyName();
        final Optional<IItem> retrieved = attemtper.getItem(keyName);
        if (retrieved == null || retrieved.isEmpty()) {
            return false;
        }
        final IItem keyItem = retrieved.get();
        final UsableCapability capability = keyItem.getUsableCapability();
        if (capability == null) {
            return false;
        }
        if (!capability.hasUsesRemaining()) {
            attemtper.removeItem(keyItem);
            return false;
        }
        if (!capability.useOnce()) {
            attemtper.removeItem(keyItem);
        }
        return true;
    }

    public default boolean canAccess(InventoryOwner attempter) {
        return this.isUnlocked() || this.isAuthorized(attempter);
    }

    public default boolean accessUnlocks() {
        return true;
    }

}