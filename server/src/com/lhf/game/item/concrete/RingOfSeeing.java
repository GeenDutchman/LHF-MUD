package com.lhf.game.item.concrete;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Consumable;
import com.lhf.game.item.interfaces.Equipable;
import com.lhf.game.item.interfaces.Usable;
import com.lhf.game.map.Room;

import java.util.*;

public class RingOfSeeing extends Usable implements Equipable, Consumable {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

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

        types = new ArrayList<>();
        slots = new ArrayList<>();
        slots.add(EquipmentSlots.LEFTHAND);
        slots.add(EquipmentSlots.RIGHTHAND);
    }

    @Override
    public boolean isUsedUp() {
        return this.hasUsesLeft();
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
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public Map<String, Integer> unequip() {
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public String getDescription() {
        String result = "This ring can help you see things that are not visible to the naked eye. ";
        result += "It can only be used so many times though, and then the ring itself disappears... ";
        result += "This can be equipped to: " + printWhichSlots();
        // result += "\r\nAnd can best be used if you have these proficiencies: " +
        // printWhichTypes();
        return result;
    }

}
