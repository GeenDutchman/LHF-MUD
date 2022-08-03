package com.lhf.game.creature.inventory;

import java.util.Optional;

import com.lhf.game.Container;
import com.lhf.game.item.Item;

public interface InventoryOwner extends Container {
    Inventory getInventory();

    String printInventory();

    String useItem(String itemName, Object onWhat);

    @Override
    public default Optional<Item> getItem(String name) {
        Inventory inventory = this.getInventory();
        return inventory.getItem(name);
    }

    @Override
    default boolean addItem(Item item) {
        return this.getInventory().addItem(item);
    }

    @Override
    default boolean hasItem(String name) {
        return this.getInventory().hasItem(name);
    }

    @Override
    default Optional<Item> removeItem(String name) {
        return this.getInventory().removeItem(name);
    }

    default boolean removeItem(Item item) {
        return this.getInventory().removeItem(item);
    }

}
