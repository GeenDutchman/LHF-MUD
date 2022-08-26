package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;

public class CreatureTargetingSpell extends ISpell<CreatureEffect> {
    protected Set<CreatureEffect> effects;

    protected CreatureTargetingSpell(CreatureTargetingSpellEntry entry, Creature caster) {
        super(entry, caster);
        this.effects = null;
    }

    private CreatureTargetingSpellEntry getTypedEntry() {
        return (CreatureTargetingSpellEntry) this.entry;
    }

    public boolean isSingleTarget() {
        return this.getTypedEntry().isSingleTarget();
    }

    @Override
    public Set<CreatureEffect> getEffects() {
        if (this.effects == null) {
            this.effects = new HashSet<>();
            for (EntityEffectSource source : this.getEntry().getEffectSources()) {
                if (source instanceof CreatureEffectSource) {
                    this.effects.add(new CreatureEffect((CreatureEffectSource) source, this.getCaster(), this));
                }
            }
        }
        return this.effects;
    }

    @Override
    public Iterator<CreatureEffect> iterator() {
        return this.getEffects().iterator();
    }

}
