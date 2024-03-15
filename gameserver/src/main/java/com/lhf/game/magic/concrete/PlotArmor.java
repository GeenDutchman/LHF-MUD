package com.lhf.game.magic.concrete;

import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEventTester;

public class PlotArmor extends CreatureTargetingSpellEntry {

    public static final String name = "Plot Armor";

    private static final Set<CreatureEffectSource> spellEffects = Set.of(
            new CreatureEffectSource.Builder(name)
                    .setPersistence(new EffectPersistence(1, new GameEventTester(
                            GameEventType.EQUIP,
                            Set.of("You successfully equipped your",
                                    EquipmentSlots.ARMOR.getColorTaggedName()),
                            null, TickType.CONDITIONAL)))
                    .setDescription("Effects of the blessing").setOnApplication(new Deltas()
                            .setStatChange(Stats.MAXHP, 200)
                            .setStatChange(Stats.MAXHP, 200)
                            .setStatChange(Stats.CURRENTHP, 200)
                            .setStatChange(Stats.PROFICIENCYBONUS, 5)
                            .setAttributeScoreChange(Attributes.STR, 20)
                            .setAttributeScoreChange(Attributes.DEX, 20)
                            .setAttributeScoreChange(Attributes.CHA, 20)
                            .setAttributeScoreChange(Attributes.CON, 20))
                    .build());

    public PlotArmor() {
        super(ResourceCost.TENTH_MAGNITUDE, PlotArmor.name, "I bless you", PlotArmor.spellEffects,
                Set.of(VocationName.DUNGEON_MASTER),
                "Blesses the target with extra stats and attributes, and does extra stuff for NPC's.",
                true);

    }

}
