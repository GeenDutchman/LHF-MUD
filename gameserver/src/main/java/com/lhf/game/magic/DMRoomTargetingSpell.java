package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.map.DMRoomEffect;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.game.map.RoomEffectSource;

public class DMRoomTargetingSpell extends ISpell<DMRoomEffect> {
    protected Map<String, Vocation> usernamesToEnsoul;
    protected Set<String> playersToSendOff;
    protected RoomTargetingSpell inner;
    protected Set<DMRoomEffect> effects;

    public DMRoomTargetingSpell(DMRoomTargetingSpellEntry entry, ICreature caster) {
        super(entry, caster);
        this.usernamesToEnsoul = new TreeMap<>();
        this.playersToSendOff = new TreeSet<>();
        this.inner = new RoomTargetingSpell(entry, caster);
    }

    public DMRoomTargetingSpellEntry getTypedEntry() {
        return (DMRoomTargetingSpellEntry) this.getEntry();
    }

    public DMRoomTargetingSpell addUsernameToEnsoul(String name, Vocation vocation) {
        this.usernamesToEnsoul.put(name, vocation);
        return this;
    }

    public DMRoomTargetingSpell addPlayerToSendOff(String name) {
        this.playersToSendOff.add(name);
        return this;
    }

    public Map<String, Vocation> getUsernamesToEnsoul() {
        return usernamesToEnsoul;
    }

    public Set<String> getPlayersToSendOff() {
        return playersToSendOff;
    }

    @Override
    public Set<DMRoomEffect> getEffects() {
        if (this.effects == null) {
            this.effects = new HashSet<>();
            for (EntityEffectSource source : this.getEntry().getEffectSources()) {
                DMRoomEffectSource dmRoomEffectSource;
                if (source instanceof DMRoomEffectSource correctlyTypedSource) {
                    dmRoomEffectSource = correctlyTypedSource;
                } else if (source instanceof RoomEffectSource) {
                    dmRoomEffectSource = DMRoomEffectSource.fromRoomEffectSource((RoomEffectSource) source); // already
                                                                                                             // copies
                } else {
                    continue;
                }

                if (dmRoomEffectSource.isEnsoulsUserAndSend()) {
                    for (String name : this.usernamesToEnsoul.keySet()) {
                        this.effects.add(
                                new DMRoomEffect(dmRoomEffectSource, caster, this, name,
                                        this.usernamesToEnsoul.get(name)));
                    }
                } else {
                    this.effects.add(new DMRoomEffect(dmRoomEffectSource, caster, this, null, null));
                }
            }
        }
        return this.effects;
    }

    @Override
    public boolean isOffensive() {
        return super.isOffensive() && this.inner.isOffensive();
    }

    @Override
    public Iterator<DMRoomEffect> iterator() {
        return this.getEffects().iterator();
    }

}
