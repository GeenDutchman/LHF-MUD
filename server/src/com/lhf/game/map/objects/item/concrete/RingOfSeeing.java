package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.objects.item.interfaces.Consumable;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import com.lhf.game.map.objects.item.interfaces.Usable;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class RingOfSeeing extends Usable implements Equipable, Consumable {
    public RingOfSeeing(boolean isVisible) {
        super("Ring of Seeing", isVisible, 3);
    }

    @Override
    public boolean isUsedUp() {
        return this.hasUsesLeft();
    }

    @Override
    public List<EquipmentTypes> getType() {
        List result = new ArrayList<EquipmentTypes>();
        //result.add(EquipmentTypes.something); //no type defined for ring
        return result;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        List<EquipmentSlots> result = new ArrayList<>();
        result.add(EquipmentSlots.LEFTHAND);
        result.add(EquipmentSlots.RIGHTHAND);
        return result;
    }

    @Override
    public List<Pair<String, Integer>> equip() {
        return new ArrayList<>(0); // changes nothing
    }

    @Override
    public List<Pair<String, Integer>> unequip() {
        return new ArrayList<>(0); // changes nothing
    }

    @Override
    public String getDescription() {
        String result = "This ring can help you see things that are not visible to the naked eye.\n\r";
        result += "It can only be used so many times though, and then it disappears...\n\r";
        return result;
    }
}
