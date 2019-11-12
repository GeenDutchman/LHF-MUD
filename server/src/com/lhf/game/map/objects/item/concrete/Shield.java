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

public class Shield extends Item implements Equipable {
    private int AC = 2;
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        types = Arrays.asList(EquipmentTypes.SHIELD);
        slots = Arrays.asList(EquipmentSlots.SHIELD);
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
        sj.setEmptyValue("none needed!");
        for (EquipmentTypes type : types) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    @Override
    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(",");
        sj.setEmptyValue("no slot!");
        for (EquipmentSlots slot : slots) {
            sj.add(slot.toString());
        }
        return sj.toString();
    }

    @Override
    public List<Pair<String, Integer>> equip() {
        ArrayList result = new ArrayList<Pair>();
        result.add(new Pair<String, Integer>("AC", this.AC));
        return result;
    }

    @Override
    public List<Pair<String, Integer>> unequip() {
        ArrayList result = new ArrayList<>();
        result.add(new Pair<String, Integer>("AC", -1 * this.AC));
        return result;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("This is a simple shield, it should protect you a little bit.");
        sb.append("This can be equipped to: ").append(printWhichSlots());
        //sb.append("And best used if you have these proficiencies: ").append(printWhichTypes());
        // tell how much it boosts player?
        return sb.toString();
    }
}
