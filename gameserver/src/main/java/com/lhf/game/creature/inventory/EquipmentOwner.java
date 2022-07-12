package com.lhf.game.creature.inventory;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.messages.out.OutMessage;

public interface EquipmentOwner {
    OutMessage equipItem(String itemName, EquipmentSlots slot);

    OutMessage unequipItem(EquipmentSlots slot, String weapon);

    Equipable getEquipped(EquipmentSlots slot);

    String getName();
}
