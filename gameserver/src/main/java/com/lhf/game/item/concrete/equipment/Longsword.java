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

public class Longsword extends Weapon {

    public Longsword(boolean isVisible) {
        super("Longsword", isVisible, Set.of(
                new CreatureEffectSource("Slash", new EffectPersistence(TickType.INSTANT),
                        new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC), "Swords cut things", false)
                        .addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.SLASHING))),
                DamageFlavor.SLASHING, WeaponSubtype.MARTIAL);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        this.descriptionString = "This is a nice, long, shiny sword.  It's a bit simple though...\n";
    }

    @Override
    public Longsword makeCopy() {
        return new Longsword(this.checkVisibility());
    }

}
