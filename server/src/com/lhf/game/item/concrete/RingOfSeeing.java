package com.lhf.game.item.concrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.map.Room;

public class RingOfSeeing extends Equipable {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private Map<String, Integer> equippingChanges;

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
                output += "Items you can see:\r\n";
                output += ((Room) object).printListOfAllItems();
                output += "\r\n";
                return output;
            }
            return "You cannot use a " + this.getName() + " on that.";
        });

        types = new ArrayList<>();
        slots = new ArrayList<>();
        slots.add(EquipmentSlots.LEFTHAND);
        slots.add(EquipmentSlots.RIGHTHAND);
        equippingChanges = new HashMap<>(0);
        equippingChanges.put(Attributes.WIS.toString(), 2);
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
        String result = "This ring can help you see things that are not visible to the naked eye. ";
        result += "It can only be used so many times though, and then the ring itself disappears... \n";
        result += this.printStats();
        return result;
    }

}
