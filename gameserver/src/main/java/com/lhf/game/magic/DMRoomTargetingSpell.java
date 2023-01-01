package com.lhf.game.magic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.item.Item;
import com.lhf.game.map.DMRoomEffect;
import com.lhf.game.map.DMRoomEffectSource;
import com.lhf.game.map.RoomEffectSource;

public class DMRoomTargetingSpell extends ISpell<DMRoomEffect> {
    protected Map<String, Vocation> usernamesToEnsoul;
    protected Set<String> playersToSendOff;
    protected RoomTargetingSpell inner;
    protected Set<DMRoomEffect> effects;

    public DMRoomTargetingSpell(DMRoomTargetingSpellEntry entry, Creature caster) {
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

    public DMRoomTargetingSpell addItemToSummon(Item item) {
        this.inner.addItemToSummon(item);
        return this;
    }

    public DMRoomTargetingSpell addItemToBanish(Item item) {
        this.inner.addItemToBanish(item);
        return this;
    }

    public DMRoomTargetingSpell addCreatureToSummon(Creature creature) {
        this.inner.addCreatureToSummon(creature);
        return this;
    }

    public DMRoomTargetingSpell addCreatureToBanish(Creature creature) {
        this.inner.addCreatureToBanish(creature);
        return this;
    }

    public List<Item> getItemsToSummon() {
        return this.inner.getItemsToSummon();
    }

    public List<Item> getItemsToBanish() {
        return this.inner.getItemsToBanish();
    }

    public Set<Creature> getCreaturesToSummon() {
        return this.inner.getCreaturesToSummon();
    }

    public Set<Creature> getCreaturesToBanish() {
        return this.inner.getCreaturesToBanish();
    }

    @Override
    public Set<DMRoomEffect> getEffects() {
        if (this.effects == null) {
            this.effects = new HashSet<>();
            for (EntityEffectSource source : this.getEntry().getEffectSources()) {
                DMRoomEffectSource dmRoomEffectSource;
                if (source instanceof DMRoomEffectSource) {
                    dmRoomEffectSource = (DMRoomEffectSource) source;
                } else if (source instanceof RoomEffectSource) {
                    dmRoomEffectSource = new DMRoomEffectSource((RoomEffectSource) source);
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
