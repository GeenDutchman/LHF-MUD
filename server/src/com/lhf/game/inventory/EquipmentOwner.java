package com.lhf.game.inventory;

import com.lhf.game.shared.enums.EquipmentSlots;

public interface EquipmentOwner {
    String equipItem(String itemName, EquipmentSlots slot);

    String unequipItem(EquipmentSlots slot);
}
