package com.lhf.game.enums;

import com.lhf.Taggable;

public enum EquipmentSlots implements Taggable {
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

    @Override
    public String getStartTag() {
        return "<equipSlot>";
    }

    @Override
    public String getEndTag() {
        return "</equipSlot>";
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }
}
