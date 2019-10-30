package com.lhf.game.inventory;

import com.lhf.game.shared.enums.EquipmentSlots;

public interface EquipmentOwner {
    boolean equipItem(String itemName, EquipmentSlots slot);

    void unequipItem(EquipmentSlots slot);
}
