package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoSafe;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public class CreatureAOESpell extends ISpell<CreatureEffect> {
    protected Set<CreatureEffect> effects;
    protected final AutoSafe safe;
    protected CasterVsCreatureStrategy strategy;

    protected CreatureAOESpell(CreatureAOESpellEntry entry, Creature caster, AutoSafe overrides) {
        super(entry, caster);
        this.effects = null;
        this.safe = AutoSafe.override(this.getOriginalSafe(), overrides);
        this.strategy = null;
    }

    private CreatureAOESpellEntry getTypedEntry() {
        return (CreatureAOESpellEntry) this.entry;
    }

    public AutoSafe getOriginalSafe() {
        return this.getTypedEntry().getAutoSafe();
    }

    public AutoSafe getSafe() {
        return this.safe;
    }

    public CasterVsCreatureStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategy) {
        this.strategy = strategy;
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

    @Override
    public String printDescription() {
        AutoSafe original = this.getTypedEntry().getAutoSafe();
        if (!this.safe.equals(original)) {
            return super.printDescription() + " Overriden so that " + this.safe.toString();
        }
        return super.printDescription();
    }
}
