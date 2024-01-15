package com.lhf.game.item.concrete.equipment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class RustyDagger extends Weapon {

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible, Set.of(
                new CreatureEffectSource("Stab", new EffectPersistence(TickType.INSTANT),
                        new EffectResistance(EnumSet.of(Attributes.DEX), Stats.AC),
                        "Daggers stab things", false)
                        .addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.PIERCING))),
                DamageFlavor.PIERCING, WeaponSubtype.PRECISE);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
        this.descriptionString = "Rusty Dagger to stab monsters with. \n";
    }

    @Override
    public RustyDagger makeCopy() {
        return new RustyDagger(this.isVisible());
    }

}
