package com.lhf.game.item.concrete.equipment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectResistance;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
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
    private static final String description = "Rusty Dagger to stab monsters with. \n";

    public RustyDagger() {
        super("Rusty Dagger", RustyDagger.description, Set.of(
                new CreatureEffectSource.Builder("Stab").instantPersistence()
                        .setResistance(new EffectResistance(EnumSet.of(Attributes.DEX), Stats.AC))
                        .setDescription("Daggers stab things")
                        .setOnApplication(
                                new Deltas().addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.PIERCING)))
                        .build()),
                DamageFlavor.PIERCING, WeaponSubtype.PRECISE);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER);
    }

    @Override
    public RustyDagger makeCopy() {
        return this;
    }

}
