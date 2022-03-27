package com.lhf.game.creature.inventory;

import com.lhf.game.Container;

public interface InventoryOwner extends Container {
    Inventory getInventory();

    String printInventory();

    String useItem(String itemName, Object onWhat);
}
