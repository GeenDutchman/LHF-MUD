package com.lhf.game.magic;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.out.CastingMessage;

public class CreatureTargetingSpellEntry extends SpellEntry {
    protected final boolean singleTarget;

    public CreatureTargetingSpellEntry(SpellLevel level, String name, Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean singleTarget) {
        super(level, name, effectSources, allowed, description);
        this.singleTarget = singleTarget;
    }

    public CreatureTargetingSpellEntry(SpellLevel level, String name, String invocation,
            Set<CreatureEffectSource> effectSources,
            Set<VocationName> allowed, String description, boolean singleTarget) {
        super(level, name, invocation, effectSources, allowed, description);
        this.singleTarget = singleTarget;
    }

    public boolean isSingleTarget() {
        return singleTarget;
    }

    @Override
    public CastingMessage Cast(Creature caster, SpellLevel castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return CastingMessage.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString()).Build();
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
