package com.lhf.game.inventory;

import com.lhf.game.map.objects.item.interfaces.EquipType;

public interface EquipmentOwner {
    boolean equipItem(String itemName);

    void unequipItem(EquipType itemName);
}
