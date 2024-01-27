package com.lhf.game.creature.inventory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.IItem;

public interface InventoryOwner extends ItemContainer {
    Inventory getInventory();

    void setInventory(Inventory inventory);

    String printInventory();

    String getName();

    @Override
    public default Collection<IItem> getItems() {
        Inventory inventory = this.getInventory();
        return inventory.getItems();
    }

    @Override
    public default Optional<IItem> getItem(String name) {
        Inventory inventory = this.getInventory();
        return inventory.getItem(name);
    }

    @Override
    default boolean addItem(IItem item) {
        return this.getInventory().addItem(item);
    }

    @Override
    default boolean hasItem(String name) {
        return this.getInventory().hasItem(name);
    }

    @Override
    default Optional<IItem> removeItem(String name) {
        return this.getInventory().removeItem(name);
    }

    @Override
    default boolean removeItem(IItem item) {
        return this.getInventory().removeItem(item);
    }

    @Override
    default Iterator<? extends IItem> itemIterator() {
        return this.getInventory().itemIterator();
    }

}
