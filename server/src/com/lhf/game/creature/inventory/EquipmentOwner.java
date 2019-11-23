package com.lhf.game.creature.inventory;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Equipable;

public interface EquipmentOwner {
    String equipItem(String itemName, EquipmentSlots slot);

    String unequipItem(EquipmentSlots slot, String weapon);

    Equipable getEqupped(EquipmentSlots slot);
}
