package com.lhf.game.enums;

public enum EquipmentSlots {
    HAT, NECKLACE, ARMOR, SHIELD, ARM, LEFTHAND, RIGHTHAND, BELT, BOOTS, WEAPON;

    public static EquipmentSlots getEquipmentSlot(String value) throws IllegalArgumentException {
        for (EquipmentSlots slot : values()) {
            if (slot.toString().equalsIgnoreCase(value)) {
                return slot;
            }
        }

        throw new IllegalArgumentException("Bad value '" + value + "' for " + EquipmentSlots.class.toString());
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
