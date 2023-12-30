package com.lhf.game.magic.concrete;

import java.util.EnumSet;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.magic.RoomTargetingSpellEntry;
import com.lhf.game.map.RoomEffectSource;

public class ElectricWisp extends RoomTargetingSpellEntry {
    private static final Set<RoomEffectSource> spellEffects = Set.of(new RoomEffectSource("Summon Electric Wisp",
            new EffectPersistence(2, TickType.ROUND), new EffectResistance(Attributes.WIS, 5, TargetResistAmount.ALL),
            "Summons an Electric Wisp to fight by your side!")
            .setCreatureToSummon(MonsterBuilder.getInstance()
                    .setStatblock(new Statblock(null, null, null, null, null, null, null))));

    public ElectricWisp() {
        super(ResourceCost.FIRST_MAGNITUDE, "Summon Electric Wisp", "nosk aklo Astra", spellEffects,
                EnumSet.of(VocationName.HEALER), "Summons an Electric Wisp to fight by your side");
    }
}
