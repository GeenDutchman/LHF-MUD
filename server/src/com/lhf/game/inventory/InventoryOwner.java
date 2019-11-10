package com.lhf.game.inventory;

import com.lhf.game.map.objects.item.interfaces.Takeable;

import java.util.Optional;

public interface InventoryOwner {
    void takeItem(Takeable item);

    Optional<Takeable> dropItem(String itemName);

    Inventory getInventory();

    String listInventory();

    void useItem(String itemName);
}
