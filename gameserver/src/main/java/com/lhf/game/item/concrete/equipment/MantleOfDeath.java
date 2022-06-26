package com.lhf.game.item.concrete.equipment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class MantleOfDeath extends Equipable {
    private int AC = 10;
    private int MAX_HEALTH = 100;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private Map<String, Integer> equippingChanges;

    public MantleOfDeath(boolean isVisible) {
        super("Mantle Of Death", isVisible);

        slots = Arrays.asList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        equippingChanges = new HashMap<>();
        equippingChanges.put(Stats.AC.toString(), this.AC);
        equippingChanges.put(Stats.MAXHP.toString(), this.MAX_HEALTH);
        equippingChanges.put(Stats.CURRENTHP.toString(), this.MAX_HEALTH);
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
    public String printDescription() {
        StringBuilder sb = new StringBuilder(
                "This fearsome hooded robe seems a little bit overpowered to be in your puny hands. \n");
        sb.append(this.printStats());
        return sb.toString();
    }
}
