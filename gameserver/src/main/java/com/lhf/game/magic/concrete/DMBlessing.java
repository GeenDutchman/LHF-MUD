package com.lhf.game.magic.concrete;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureTargetingSpellEntry;

public class DMBlessing extends CreatureTargetingSpellEntry {

    public static final String name = "DMBlessing";

    public DMBlessing() {
        super(10, DMBlessing.name, "I bless you", new EffectPersistence(TickType.CONDITIONAL),
                "Blesses the target with extra stats and attributes, and does extra stuff for NPC's.",
                true, false, VocationName.DUNGEON_MASTER);

        this.statChanges.put(Stats.MAXHP, 200);
        this.statChanges.put(Stats.CURRENTHP, 200);
        this.statChanges.put(Stats.PROFICIENCYBONUS, 5);
        this.attributeScoreChanges.put(Attributes.STR, 20);
        this.attributeScoreChanges.put(Attributes.DEX, 20);
        this.attributeScoreChanges.put(Attributes.CHA, 20);
        this.attributeScoreChanges.put(Attributes.CON, 20);
    }

}
