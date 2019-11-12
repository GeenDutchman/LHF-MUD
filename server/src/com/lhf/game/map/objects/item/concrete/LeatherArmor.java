package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class LeatherArmor extends Item implements Equipable {
    private int AC = 2;

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public LeatherArmor(boolean isVisible) {
        super("Leather Armor", isVisible);

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
    public String printWhichTypes() {
        StringJoiner sj = new StringJoiner(",");
        for (EquipmentTypes type : types) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    @Override
    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(",");
        for (EquipmentSlots slot : slots) {
            sj.add(slot.toString());
        }
        return sj.toString();
    }

    @Override
    public List<Pair<String, Integer>> equip() {
        ArrayList<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
        result.add(new Pair<String, Integer>("AC", this.AC));
        return result;
    }

    @Override
    public List<Pair<String, Integer>> unequip() {
        ArrayList result = new ArrayList<Pair<String, Integer>>();
        result.add(new Pair<String, Integer>("AC", -1 * this.AC));
        return result;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("This is some simple leather armor.\n");
        sb.append("There is only a little blood on it...");
        sb.append("\n\rThis can be equipped to: ").append(printWhichSlots());
        sb.append("\n\rAnd best used if you have these proficiencies: ").append(printWhichTypes());
        return sb.toString();
    }
}
