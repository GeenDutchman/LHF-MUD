package com.lhf.game.inventory;

import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.Equipable;

import java.util.HashMap;
import java.util.Optional;

public class Equipment {
    private HashMap<EquipType, Equipable> equipped;

    public Equipment() {
        equipped = new HashMap<>();
    }

    public Optional<Equipable> equipItem(Equipable item) {
        Optional<Equipable> oldItem = Optional.ofNullable(this.equipped.remove(item.getType()));
        this.equipped.put(item.getType(), item);
        return oldItem;
    }

    public Optional<Equipable> unequip(EquipType type) {
        return Optional.ofNullable(this.equipped.remove(type));
    }
}
