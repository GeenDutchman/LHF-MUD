package com.lhf.game.inventory;

import com.lhf.game.map.objects.item.interfaces.Takeable;

public interface InventoryOwner {
    void takeItem(Takeable item);

    Inventory getInventory();

    String listInventory();

    void useItem(String itemName);
}
