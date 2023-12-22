package com.lhf.game.magic;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.messages.events.CastingEvent;

public class CreatureTargetingSpellEntry extends SpellEntry {
    protected final boolean singleTarget;

    public CreatureTargetingSpellEntry(ResourceCost level, String name, Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean singleTarget) {
        super(level, name, effectSources, allowed, description);
        this.singleTarget = singleTarget;
    }

    public CreatureTargetingSpellEntry(ResourceCost level, String name, String invocation,
            Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean singleTarget) {
        super(level, name, invocation, effectSources, allowed, description);
        this.singleTarget = singleTarget;
    }

    public boolean isSingleTarget() {
        return singleTarget;
    }

    @Override
    public CastingEvent Cast(ICreature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return CastingEvent.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString()).Build();
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.description);
        if (this.singleTarget) {
            sj.add("Targets only one creature.");
        } else {
            sj.add("Can target multiple creatures.");
        }
        sj.add("\r\n");

        return sj.toString() + super.printEffectDescriptions();
    }
}
