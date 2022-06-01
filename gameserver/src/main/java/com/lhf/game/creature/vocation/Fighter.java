package com.lhf.game.creature.vocation;

import java.util.HashSet;

import com.lhf.game.enums.EquipmentTypes;

public class Fighter extends Vocation {

    public Fighter() {
        super("Fighter");
        if (this.proficiencies == null) {
            this.proficiencies = new HashSet<>();
        }
        this.proficiencies.add(EquipmentTypes.LIGHTARMOR);
        this.proficiencies.add(EquipmentTypes.MEDIUMARMOR);
        this.proficiencies.add(EquipmentTypes.SHIELD);
        this.proficiencies.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        this.proficiencies.add(EquipmentTypes.MARTIALWEAPONS);
    }

}
