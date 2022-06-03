package com.lhf.game.creature.vocation;

import java.util.HashSet;

import com.lhf.game.enums.EquipmentTypes;

public class Fighter extends Vocation {

    public Fighter() {
        super("Fighter", Fighter.generateProficiencies());
    }

    private static HashSet<EquipmentTypes> generateProficiencies() {
        HashSet<EquipmentTypes> prof = new HashSet<>();
        prof.add(EquipmentTypes.LIGHTARMOR);
        prof.add(EquipmentTypes.MEDIUMARMOR);
        prof.add(EquipmentTypes.SHIELD);
        prof.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        prof.add(EquipmentTypes.MARTIALWEAPONS);
        return prof;
    }

}
