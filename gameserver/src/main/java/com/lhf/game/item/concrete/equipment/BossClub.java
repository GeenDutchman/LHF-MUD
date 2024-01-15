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

public class BossClub extends Weapon {

    public BossClub(boolean isVisible) {
        super("Boss Club", isVisible, Set.of(
                new CreatureEffectSource("Bash", new EffectPersistence(TickType.INSTANT),
                        new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC),
                        "Club it like a boss.", false)
                        .addDamage(new DamageDice(2, DieType.EIGHT, DamageFlavor.BLUDGEONING))),
                DamageFlavor.BLUDGEONING, WeaponSubtype.MARTIAL);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        this.descriptionString = "This is a large club, it seems a bit rusty... wait that is not rust...\n";

    }

    @Override
    public BossClub makeCopy() {
        return new BossClub(this.isVisible());
    }

}
