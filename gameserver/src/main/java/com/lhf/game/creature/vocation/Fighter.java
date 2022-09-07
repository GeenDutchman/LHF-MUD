package com.lhf.game.creature.vocation;

import java.util.EnumSet;

import com.lhf.game.enums.EquipmentTypes;

public class Fighter extends Vocation {

    public Fighter() {
        super(VocationName.FIGHTER);
    }

    @Override
    protected EnumSet<EquipmentTypes> generateProficiencies() {
        EnumSet<EquipmentTypes> prof = EnumSet.noneOf(EquipmentTypes.class);
        prof.add(EquipmentTypes.LIGHTARMOR);
        prof.add(EquipmentTypes.MEDIUMARMOR);
        prof.add(EquipmentTypes.SHIELD);
        prof.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        prof.add(EquipmentTypes.MARTIALWEAPONS);
        return prof;
    }

}
