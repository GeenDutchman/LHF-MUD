package com.lhf.game.item.concrete.equipment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectResistance;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class ReaperScythe extends Weapon {
    private final static String description = "This is a nice, long, shiny scythe.  It's super powerful...\n";

    public ReaperScythe() {
        super("Reaper Scythe", ReaperScythe.description, Set.of(
                new CreatureEffectSource.Builder("Scythe").instantPersistence()
                        .setResistance(new EffectResistance(
                                EnumSet.of(Attributes.STR, Attributes.DEX), Stats.AC))
                        .setDescription("Scythes reap things.")
                        .setOnApplication(new Deltas().addDamage(new DamageDice(1,
                                DieType.EIGHT, DamageFlavor.NECROTIC)))
                        .build()),
                DamageFlavor.NECROTIC, WeaponSubtype.FINESSE);
        this.setVisible(false);

        this.slots = List.of(EquipmentSlots.WEAPON);
        this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
    }

    @Override
    public Attack generateAttack(ICreature attacker) {
        int actorDC = 10 + (attacker.getStats().getOrDefault(Stats.MAXHP, 0)
                - attacker.getStats().getOrDefault(Stats.CURRENTHP, 0));
        Set<CreatureEffectSource> extraSources = Set
                .of(new CreatureEffectSource.Builder("Necrotic Damage").instantPersistence()
                        .setResistance(new EffectResistance(null, null, actorDC, null, Stats.AC,
                                null,
                                TargetResistAmount.HALF))
                        .setDescription("This weapon does extra necrotic damage.")
                        .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, -100))
                        .build());

        return super.generateAttack(attacker, extraSources);
    }

    @Override
    public ReaperScythe makeCopy() {
        return this;
    }

}
