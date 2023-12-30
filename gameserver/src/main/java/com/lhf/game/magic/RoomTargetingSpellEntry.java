package com.lhf.game.magic;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.events.SpellCastingEvent;

public class RoomTargetingSpellEntry extends SpellEntry {

    public RoomTargetingSpellEntry(ResourceCost level, String name, String invocation,
            Set<? extends RoomEffectSource> effectSources,
            Set<VocationName> allowed, String description) {
        super(level, name, invocation, effectSources, allowed, description);
    }

    public RoomTargetingSpellEntry(ResourceCost level, String name, Set<? extends RoomEffectSource> effectSources,
            Set<VocationName> allowed, String description) {
        super(level, name, effectSources, allowed, description);
    }

    @Override
    public SpellCastingEvent Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return SpellCastingEvent.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString())
                .Build();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(this.description).append(super.printEffectDescriptions());
        return sb.toString();
    }

}
