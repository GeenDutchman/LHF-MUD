package com.lhf.game.magic.concrete;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.events.SpellCastingEvent;

public class ShockBolt extends CreatureTargetingSpellEntry {
    private static final Set<CreatureEffectSource> spellEffects = Set.of(
            new CreatureEffectSource("Zap", new EffectPersistence(TickType.INSTANT),
                    new EffectResistance(EnumSet.of(Attributes.INT, Attributes.WIS, Attributes.CHA), Stats.AC),
                    "This spell zaps things.", false)
                    .addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.LIGHTNING)));

    public ShockBolt() {
        super(ResourceCost.NO_COST, "Shock Bolt", "Astra Horeb", spellEffects,
                Set.of(),
                "A small spark of electricity shocks a creature you choose as a target", true);
    }

    @Override
    public SpellCastingEvent Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        for (Taggable target : targets) {
            sb.append("A small spark zips from").append(caster.getColorTaggedName())
                    .append("'s finger and flies toward").append(target.getColorTaggedName()).append("!");
        }
        return SpellCastingEvent.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sb.toString())
                .Build();
    }

}
