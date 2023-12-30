package com.lhf.game.magic.concrete;

import java.util.EnumSet;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.creature.INonPlayerCharacter.AbstractNPCBuilder.SummonData;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.DamgeFlavorReaction;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.game.map.RoomEffectSource;

public class ElectricWisp extends RoomTargetingSpellEntry {
    private static final Set<RoomEffectSource> spellEffects = Set.of(new RoomEffectSource("Summon Electric Wisp",
            new EffectPersistence(2, TickType.ROUND), new EffectResistance(Attributes.WIS, 5, TargetResistAmount.ALL),
            "Summons an Electric Wisp to fight by your side!")
            .setCreatureToSummon(MonsterBuilder.getInstance()
                    .setStatblock(Statblock.getBuilder().setCreatureRace("Wisp").setAttributeBlock(1, 12, 1, 16, 10, 16)
                            .addFlavorReaction(DamgeFlavorReaction.IMMUNITIES, DamageFlavor.LIGHTNING)
                            .addFlavorReaction(DamgeFlavorReaction.WEAKNESSES, DamageFlavor.PSYCHIC).build())
                    .setSummonStates(Set.of(SummonData.LIFELINE_SUMMON, SummonData.SYMPATHETIC_SUMMON))));

    public ElectricWisp() {
        super(ResourceCost.FIRST_MAGNITUDE, "Summon Electric Wisp", "nosk aklo Astra", spellEffects,
                EnumSet.of(VocationName.HEALER), "Summons an Electric Wisp to fight by your side");
    }
}
