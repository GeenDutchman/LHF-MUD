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

public class Longsword extends Weapon {
    private final static String description = "This is a nice, long, shiny sword.  It's a bit simple though...\n";

    public Longsword() {
        super("Longsword", Longsword.description, Set.of(
                new CreatureEffectSource.Builder("Slash").instantPersistence()
                        .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                        .setDescription("Swords cut things")
                        .setOnApplication(
                                new Deltas().addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.SLASHING)))
                        .build()),
                DamageFlavor.SLASHING, WeaponSubtype.MARTIAL);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
    }

    @Override
    public Longsword makeCopy() {
        return this;
    }

}
