package com.lhf.messages.in;

import com.lhf.game.shared.enums.EquipmentSlots;

import static com.lhf.game.shared.enums.EquipmentSlots.*;

public class UnequipMessage extends InMessage {
    EquipmentSlots equipSlot;

    public UnequipMessage(String args) {
        String cmd = args.trim();
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


    public EquipmentSlots getEquipSlot() {
        return equipSlot;
    }
}
