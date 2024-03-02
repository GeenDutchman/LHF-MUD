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

public class BossClub extends Weapon {
    private final static String description = "This is a large club, it seems a bit rusty... wait that is not rust...\n";

    public BossClub() {
        super("Boss Club", BossClub.description, Set.of(
                new CreatureEffectSource.Builder("Bash").instantPersistence()
                        .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                        .setDescription("Club it like a boss.")
                        .setOnApplication(
                                new Deltas().addDamage(new DamageDice(2, DieType.EIGHT, DamageFlavor.BLUDGEONING)))
                        .build()),
                DamageFlavor.BLUDGEONING, WeaponSubtype.MARTIAL);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);

    }

    @Override
    public BossClub makeCopy() {
        return this;
    }

}
