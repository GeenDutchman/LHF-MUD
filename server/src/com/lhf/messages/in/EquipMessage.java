package com.lhf.messages.in;

import com.lhf.game.shared.enums.EquipmentSlots;

import static com.lhf.game.shared.enums.EquipmentSlots.*;

public class EquipMessage extends InMessage {
    String itemName = "";
    EquipmentSlots equipSlot;

    static final private String[] prepositionFlags = {"to"};

    public EquipMessage(String args) {
        itemName = args.substring(0, args.lastIndexOf(' '));
        String cmd = args.substring(args.lastIndexOf(' ') + 1).trim(); //equip item_name slot
        if (itemName.contains(' ' + prepositionFlags[0])) { //equip item_name to slot
            //TODO: check this logic
            itemName = itemName.substring(0, itemName.lastIndexOf(' ' + prepositionFlags[0]));
        }
        switch (cmd.toLowerCase()) {
            case "hat":
                equipSlot = HAT;
                break;
            case "necklace":
                equipSlot = NECKLACE;
                break;
            case "armor":
                equipSlot = ARMOR;
                break;
            case "shield":
                equipSlot = SHIELD;
                break;
            case "arm":
                equipSlot = ARM;
                break;
            case "lefthand":
                equipSlot = LEFTHAND;
                break;
            case "righthand":
                equipSlot = RIGHTHAND;
                break;
            case "belt":
                equipSlot = BELT;
                break;
            case "boots":
                equipSlot = BOOTS;
                break;
            case "weapon":
                equipSlot = WEAPON;
                break;
            default:
                equipSlot = null;
        }
    }

    public String getItemName() {
        return itemName;
    }

    public EquipmentSlots getEquipSlot() {
        return equipSlot;
    }
}
