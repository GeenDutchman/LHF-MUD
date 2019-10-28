package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.IEquipable;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class LeatherArmor implements IEquipable {
    private int AC = 2;

    @Override
    public EquipType getType() {
        return EquipType.BODY;
    }

    @Override
    public List<Pair<String, Integer>> equip() {
        ArrayList result = new ArrayList<Pair<String, Integer>>();
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
        return sb.toString();
    }
}
