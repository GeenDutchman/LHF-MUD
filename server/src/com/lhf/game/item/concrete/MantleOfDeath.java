package com.lhf.game.item.concrete;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

public class MantleOfDeath extends Item implements Equipable {
    private int AC = 10;
    private int MAX_HEALTH = 100;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public MantleOfDeath(boolean isVisible) {
        super("Mantle Of Death", isVisible);

        slots = Arrays.asList(EquipmentSlots.ARMOR);
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
    public Map<String, Integer> equip() {
        Map<String, Integer> result = new HashMap<>();
        result.put(Stats.AC.toString(), this.AC);
        result.put(Stats.MAXHP.toString(), this.MAX_HEALTH);
        result.put(Stats.CURRENTHP.toString(), this.MAX_HEALTH);
        return result;
    }

    @Override
    public Map<String, Integer> unequip() {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", -1 * this.AC);
        result.put(Stats.MAXHP.toString(), -1 * this.MAX_HEALTH);
        result.put(Stats.CURRENTHP.toString(), -1 * this.MAX_HEALTH);
        return result;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(
                "This fearsome hooded robe seems a little bit overpowered to be in your puny hands. ");
        sb.append("This can be equipped to: ").append(printWhichSlots());
        // sb.append("And best used if you have these proficiencies:
        // ").append(printWhichTypes());
        return sb.toString();
    }
}
