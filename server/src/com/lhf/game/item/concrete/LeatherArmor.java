package com.lhf.game.item.concrete;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.creature.inventory.EquipmentOwner;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

public class LeatherArmor extends Item implements Equipable {
    private int AC = 2;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public LeatherArmor(boolean isVisible) {
        super("Leather Armor", isVisible);

        slots = Collections.singletonList(EquipmentSlots.ARMOR);
        types = Arrays.asList(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
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
    public Map<String, Integer> onEquippedBy(EquipmentOwner newOwner) {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", this.AC);
        return result;
    }

    @Override
    public Map<String, Integer> onUnequippedBy(EquipmentOwner disowner) {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", -1 * this.AC);
        return result;
    }

    @Override
    public String getDescription() {
        return "This is some simple leather armor. " + "There is only a little blood on it...\n" +
                this.printStats();
    }
}
