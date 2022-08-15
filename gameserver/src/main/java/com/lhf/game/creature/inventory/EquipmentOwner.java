package com.lhf.game.creature.inventory;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Equipable;

public interface EquipmentOwner {
    boolean equipItem(String itemName, EquipmentSlots slot);

    boolean unequipItem(EquipmentSlots slot, String weapon);

    Equipable getEquipped(EquipmentSlots slot);

    String getName();
}
