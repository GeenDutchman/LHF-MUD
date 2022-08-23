package com.lhf.game.magic.concrete;

import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureTargetingSpellEntry;

public class DMBlessing extends CreatureTargetingSpellEntry {

    public static final String name = "DMBlessing";

    private static final Set<CreatureEffectSource> spellEffects = Set.of(
            new CreatureEffectSource("DMBlessing", new EffectPersistence(TickType.CONDITIONAL),
                    "Effects of the blessing", false)
                    .addStatChange(Stats.MAXHP, 200)
                    .addStatChange(Stats.MAXHP, 200)
                    .addStatChange(Stats.CURRENTHP, 200)
                    .addStatChange(Stats.PROFICIENCYBONUS, 5)
                    .addAttributeScoreChange(Attributes.STR, 20)
                    .addAttributeScoreChange(Attributes.DEX, 20)
                    .addAttributeScoreChange(Attributes.CHA, 20)
                    .addAttributeScoreChange(Attributes.CON, 20));

    public DMBlessing() {
        super(10, DMBlessing.name, "I bless you", DMBlessing.spellEffects, Set.of(VocationName.DUNGEON_MASTER),
                "Blesses the target with extra stats and attributes, and does extra stuff for NPC's.",
                true);

    }

}
