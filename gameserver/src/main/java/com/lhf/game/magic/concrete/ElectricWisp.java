package com.lhf.game.magic.concrete;

import java.util.EnumSet;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.TickType;
import com.lhf.game.creature.MonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo.SummonData;
import com.lhf.game.creature.vocation.Mage;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.game.map.RoomEffectSource;

public class ElectricWisp extends RoomTargetingSpellEntry {
    private static final Set<RoomEffectSource> spellEffects = Set
            .of(new RoomEffectSource.Builder("Summon Electric Wisp")
                    .setPersistence(new EffectPersistence(2, TickType.ROUND))
                    .setResistance(new EffectResistance(Attributes.WIS, 5, TargetResistAmount.ALL))
                    .setDescription("Summons an Electric Wisp to fight by your side!")
                    .setMonsterToSummon(MonsterBuildInfo.getInstance().setVocation(new Mage())
                            .setCreatureRace("Wisp").setAttributeBlock(1, 12, 1, 16, 10, 16)
                            .addFlavorReaction(DamgeFlavorReaction.IMMUNITIES,
                                    DamageFlavor.LIGHTNING)
                            .addFlavorReaction(DamgeFlavorReaction.WEAKNESSES,
                                    DamageFlavor.PSYCHIC)
                            .setSummonStates(Set.of(SummonData.LIFELINE_SUMMON,
                                    SummonData.SYMPATHETIC_SUMMON)))
                    .build());

    public ElectricWisp() {
        super(ResourceCost.FIRST_MAGNITUDE, "Summon Electric Wisp", "nosk aklo Astra", spellEffects,
                EnumSet.of(VocationName.HEALER), "Summons an Electric Wisp to fight by your side");
    }
}
