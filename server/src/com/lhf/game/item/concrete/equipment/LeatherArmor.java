package com.lhf.game.item.concrete.equipment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class LeatherArmor extends Equipable {
    private int AC = 2;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private Map<String, Integer> equippingChanges;

    public LeatherArmor(boolean isVisible) {
        super("Leather Armor", isVisible);

        slots = Collections.singletonList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
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
        return "This is some simple leather armor. " + "There is only a little blood on it...\n" +
                this.printStats();
    }
}
