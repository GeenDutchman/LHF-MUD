package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.map.Room;
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
        this.setUseAction(Room.class.getName(), (object) -> {
            if (object == null) {
                return "That is not a valid target at all!";
            } else if (object instanceof Room) {
                String output = getDescription() +
                        "\r\n" +
                        "The possible directions are:\r\n";
                output += ((Room) object).getDirections();
                output += "\r\n";
                output += "Objects you can see:\r\n";
                output += ((Room) object).getListOfAllObjects();
                output += "\r\n";
                output += "Items you can see:\r\n";
                output += ((Room) object).getListOfAllItems();
                output += "\r\n";
                return output;
            }
            return "You cannot use a " + this.getName() + " on that.";
        });
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
        result += "It can only be used so many times though, and then the ring itself disappears...\n\r";
        return result;
    }


}
