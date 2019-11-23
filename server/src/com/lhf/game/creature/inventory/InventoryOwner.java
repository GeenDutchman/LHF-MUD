package com.lhf.game.creature.inventory;

import com.lhf.game.item.interfaces.Takeable;

import java.util.Optional;

public interface InventoryOwner {
    void takeItem(Takeable item);

    Optional<Takeable> dropItem(String itemName);

    Inventory getInventory();

    String listInventory();

    String useItem(String itemName, Object onWhat);
}
