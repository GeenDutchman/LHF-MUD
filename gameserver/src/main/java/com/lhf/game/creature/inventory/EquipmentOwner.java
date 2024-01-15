package com.lhf.game.creature.inventory;

import java.util.EnumMap;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemVisitor;

public interface EquipmentOwner {
    boolean equipItem(String itemName, EquipmentSlots slot);

    boolean unequipItem(EquipmentSlots slot, String weapon);

    Equipable getEquipped(EquipmentSlots slot);

    String getName();

    Map<EquipmentSlots, Equipable> getEquipmentSlots();

    public default void acceptItemVisitor(ItemVisitor visitor) {
        for (Item item : this.getEquipmentSlots().values()) {
            if (item == null) {
                continue;
            }
            item.acceptItemVisitor(visitor);
        }
    }

    void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots);
}
