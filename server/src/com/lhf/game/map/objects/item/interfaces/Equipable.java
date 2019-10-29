package com.lhf.game.map.objects.item.interfaces;

import javafx.util.Pair;

import java.util.List;

public interface Equipable extends Takeable {
    EquipType getType();

    List<Pair<String, Integer>> equip();

    List<Pair<String, Integer>> unequip();
}
