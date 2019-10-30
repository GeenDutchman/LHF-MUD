package com.lhf.game.map.objects.item.interfaces;

import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;
import javafx.util.Pair;

import java.util.List;

public interface Equipable extends Takeable {
    List<EquipmentTypes> getType();

    List<EquipmentSlots> getWhichSlots();

    List<Pair<String, Integer>> equip();

    List<Pair<String, Integer>> unequip();
}
