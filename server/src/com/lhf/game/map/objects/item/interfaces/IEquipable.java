package com.lhf.game.map.objects.item.interfaces;

import com.lhf.game.map.objects.sharedinterfaces.Examinable;
import javafx.util.Pair;

import java.util.List;

public interface IEquipable extends Examinable {
    EquipType getType();

    List<Pair<String, Integer>> equip();

    List<Pair<String, Integer>> unequip();
}
