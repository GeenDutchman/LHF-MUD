package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;

public class CreatureAOESpell extends ISpell<CreatureEffect> {
    protected Set<CreatureEffect> effects;
    protected final AutoTargeted safe;

    protected CreatureAOESpell(CreatureAOESpellEntry entry, ICreature caster, AutoTargeted overrides) {
        super(entry, caster);
        this.effects = null;
        this.safe = AutoTargeted.override(this.getOriginalSafe(), overrides, this.isOffensive());
    }

    private CreatureAOESpellEntry getTypedEntry() {
        return (CreatureAOESpellEntry) this.entry;
    }

    public AutoTargeted getOriginalSafe() {
        return this.getTypedEntry().getAutoSafe();
    }

    public AutoTargeted getSafe() {
        return this.safe;
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
        AutoTargeted original = this.getTypedEntry().getAutoSafe();
        if (!this.safe.equals(original)) {
            return super.printDescription() + " Overriden so that "
                    + (this.isOffensive() ? this.safe.printUnffected() : this.safe.printAffected());
        }
        return super.printDescription();
    }
}
