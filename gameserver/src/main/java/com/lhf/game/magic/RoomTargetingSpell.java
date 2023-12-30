package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;

public class RoomTargetingSpell extends ISpell<RoomEffect> {
    protected Set<RoomEffect> effects;

    public RoomTargetingSpell(RoomTargetingSpellEntry entry, ICreature caster) {
        super(entry, caster);
    }

    public RoomTargetingSpellEntry getTypedEntry() {
        return (RoomTargetingSpellEntry) this.getEntry();
    }

    @Override
    public Iterator<RoomEffect> iterator() {
        return this.getEffects().iterator();
    }

    @Override
    public Set<RoomEffect> getEffects() {
        if (this.effects == null) {
            this.effects = new HashSet<>();
            for (EntityEffectSource source : this.getEntry().getEffectSources()) {
                if (source instanceof RoomEffectSource) {
                    this.effects.add(new RoomEffect((RoomEffectSource) source, this.getCaster(), this));
                }
            }
        }
        return this.effects;
    }

}
