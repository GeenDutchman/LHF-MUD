package com.lhf.game.enums;

public enum EquipmentSlots {
    HAT, NECKLACE, ARMOR, SHIELD, ARM, LEFTHAND, RIGHTHAND, BELT, BOOTS, WEAPON;

    public static EquipmentSlots getEquipmentSlot(String value) {
        for (EquipmentSlots slot : values()) {
            if (slot.toString().equalsIgnoreCase(value)) {
                return slot;
            }
        }

        return null;
    }

    public static Boolean isEquipmentSlot(String value) {
        for (EquipmentSlots slot : values()) {
            if (slot.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
