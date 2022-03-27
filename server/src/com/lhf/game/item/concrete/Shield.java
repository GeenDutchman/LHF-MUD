package com.lhf.game.item.concrete;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class Shield extends Equipable {
    private int AC = 2;
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private Map<String, Integer> equippingChanges;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        types = Collections.singletonList(EquipmentTypes.SHIELD);
        slots = Collections.singletonList(EquipmentSlots.SHIELD);
        equippingChanges = new HashMap<>();
        equippingChanges.put(Stats.AC.toString(), this.AC);
    }

    @Override
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public Map<String, Integer> getEquippingChanges() {
        return this.equippingChanges;
    }

    @Override
    public String getDescription() {
        return "This is a simple shield, it should protect you a little bit. \n"
                + this.printStats();
    }
}
