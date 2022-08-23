package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public class CreatureTargetingSpell extends ISpell<CreatureEffect> {
    protected CasterVsCreatureStrategy strategy;
    protected Set<CreatureEffect> effects;

    protected CreatureTargetingSpell(CreatureTargetingSpellEntry entry, Creature caster) {
        super(entry, caster);
        this.strategy = null;
        this.effects = null;
    }

    private CreatureTargetingSpellEntry getTypedEntry() {
        return (CreatureTargetingSpellEntry) this.entry;
    }

    public CasterVsCreatureStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategy) {
        this.strategy = strategy;
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
