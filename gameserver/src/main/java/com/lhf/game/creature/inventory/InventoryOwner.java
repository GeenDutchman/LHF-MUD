package com.lhf.game.creature.inventory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.Item;

public interface InventoryOwner extends ItemContainer {
    Inventory getInventory();

    String printInventory();

    @Override
    public default Collection<Item> getItems() {
        Inventory inventory = this.getInventory();
        return inventory.getItems();
    }

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

    @Override
    default boolean removeItem(Item item) {
        return this.getInventory().removeItem(item);
    }

    @Override
    default Iterator<? extends Item> itemIterator() {
        return this.getInventory().itemIterator();
    }

}
