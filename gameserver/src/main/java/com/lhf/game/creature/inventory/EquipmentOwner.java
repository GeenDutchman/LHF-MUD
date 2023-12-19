package com.lhf.game.creature.inventory;

import java.util.EnumMap;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;

public interface EquipmentOwner {
    boolean equipItem(String itemName, EquipmentSlots slot);

    boolean unequipItem(EquipmentSlots slot, String weapon);

    Equipable getEquipped(EquipmentSlots slot);

    String getName();

    Map<EquipmentSlots, Equipable> getEquipmentSlots();

    void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots);
}
