package com.lhf.game.enums;

import com.lhf.Taggable;

public enum EquipmentTypes implements Taggable {
    LIGHTARMOR, MEDIUMARMOR, HEAVYARMOR, SIMPLEMELEEWEAPONS, MARTIALWEAPONS, SIMPLERANGEDWEAPONS, RANGEDWEAPONS, PADDED,
    LEATHER, STUDDEDLEATHER, HIDE, CHAINSHIRT, SCALEMAIL, BREASTPLATE, HALFPLATE, RINGMAIL, CHAINMAIL, SPLINTMAIL,
    PLATEMAIL, SHIELD, CLUB, DAGGER, GREATCLUB, HANDAXE, JAVELIN, LIGHTHAMMER, MACE, QUARTERSTAFF, SICKLE, SPEAR,
    LIGHTCROSSBOW, DART, SHORTBOW, SLING, BATTLEAXE, FLAIL, GLAIVE, GREATAXE, GREATSWORD, HALBERD, LANCE, LONGSWORD,
    MAUL, MORNINGSTAR, PIKE, RAPIER, SCIMITAR, SHORTSWORD, TRIDENT, WARPICK, WARHAMMER, WHIP, BLOWGUN, HEAVYCROSSBOW,
    HANDCROSSBOW, LONGBOW, NET, WARLOCK, MONSTERPART;

    public static EquipmentTypes getEquipmentType(String type) {
        for (EquipmentTypes equipmentTypes : values()) {
            if (equipmentTypes.toString().equalsIgnoreCase(type)) {
                return equipmentTypes;
            }
        }
        return null;
    }

    public static Boolean isEquipmentType(String type) {
        return EquipmentTypes.getEquipmentType(type) != null;
    }

    @Override
    public String getStartTag() {
        return "<equipType>";
    }

    @Override
    public String getEndTag() {
        return "</equipType>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.name() + this.getEndTag();
    }

}
