package com.lhf.messages.in;

import com.lhf.game.shared.enums.EquipmentSlots;

import static com.lhf.game.shared.enums.EquipmentSlots.*;

public class EquipMessage extends InMessage {
    String itemName;
    EquipmentSlots equipSlot;
    public EquipMessage(String args) {
        itemName = args.substring(0, args.lastIndexOf(' '));
        String cmd = args.substring(args.lastIndexOf(' ') + 1).trim();
        switch (cmd.toLowerCase()){
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
            default:
                equipSlot = null;
        }
    }

    public String getItemName() {
        return itemName;
    }

    public EquipmentSlots getEquipSlot(){ return equipSlot; }
}
