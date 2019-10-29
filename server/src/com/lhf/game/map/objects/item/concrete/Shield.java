package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Shield extends Item implements Equipable {
    private int AC = 2;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
    }

    @Override
    public EquipType getType() {
        return EquipType.HAND;
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
        // tell how much it boosts player?
        return sb.toString();
    }
}
