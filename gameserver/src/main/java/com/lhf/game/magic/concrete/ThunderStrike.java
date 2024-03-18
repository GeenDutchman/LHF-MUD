package com.lhf.game.magic.concrete;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.Taggable;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureTargetingSpellEntry;
import com.lhf.messages.events.SpellCastingEvent;

public class ThunderStrike extends CreatureTargetingSpellEntry {

    private static final Set<CreatureEffectSource> spellEffects = Set.of(
            new CreatureEffectSource.Builder("Loud Zap").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.INT), Stats.AC))
                    .setDescription("Zaps your target").setOnApplication(new Deltas()
                            .addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.THUNDER))
                            .addDamage(new DamageDice(1, DieType.FOUR,
                                    DamageFlavor.LIGHTNING)))
                    .build());

    public ThunderStrike() {
        super(ResourceCost.FIRST_MAGNITUDE, "Thunder Strike", "Bonearge Laarzen", spellEffects,
                Set.of(VocationName.MAGE),
                "A small but loud bolt of electricity shocks a creature you choose as a target",
                true);
    }

    @Override
    public SpellCastingEvent Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringBuilder sb = new StringBuilder();
        for (Taggable target : targets) {
            sb.append("A large bolt zaps from ").append(caster.getColorTaggedName())
                    .append("'s hand and thunders toward ").append(target.getColorTaggedName())
                    .append("!");
        }
        return SpellCastingEvent.getBuilder().setCaster(caster).setSpellEntry(this)
                .setCastEffects(sb.toString())
                .Build();
    }

}
