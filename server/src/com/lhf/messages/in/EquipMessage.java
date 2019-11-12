package com.lhf.messages.in;

import com.lhf.game.shared.enums.EquipmentSlots;

import static com.lhf.game.shared.enums.EquipmentSlots.*;

public class EquipMessage extends InMessage {
    String itemName = "";
    EquipmentSlots equipSlot;

    static final private String[] prepositionFlags = {"to"};

    public EquipMessage(String args) {
        String[] words = args.split(" ");
        boolean usedFlags = areFlags(words, prepositionFlags);
        String slotCmd = "";
        if (usedFlags) {
            words = prepositionSeparator(words, prepositionFlags, 2);
            this.itemName += words[0];
            slotCmd += words[1];
        } else {
            this.itemName = args;
        }

        switch (slotCmd.toLowerCase()) {
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
