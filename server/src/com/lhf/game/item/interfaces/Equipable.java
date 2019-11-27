package com.lhf.game.item.interfaces;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;

import java.util.List;
import java.util.Map;

public interface Equipable extends Takeable {

    List<EquipmentTypes> getTypes();

    List<EquipmentSlots> getWhichSlots();

    String printWhichTypes();

    String printWhichSlots();

    Map<String, Integer> equip();

    Map<String, Integer> unequip();
}
