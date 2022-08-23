package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;

public class RoomTargetingSpell extends ISpell<RoomEffect> {
    protected Set<RoomEffect> effects;
    protected List<Item> itemsToSummon;
    protected List<Item> itemsToBanish;
    protected Set<Creature> creaturesToSummon;
    protected Set<Creature> creaturesToBanish;

    private void init() {
        this.itemsToSummon = new ArrayList<>();
        this.itemsToBanish = new ArrayList<>();
        this.creaturesToSummon = new TreeSet<>();
        this.creaturesToBanish = new TreeSet<>(); // TODO: add resist
    }

    public RoomTargetingSpell(RoomTargetingSpellEntry entry, Creature caster) {
        super(entry, caster);
        this.init();
    }

    public RoomTargetingSpellEntry getTypedEntry() {
        return (RoomTargetingSpellEntry) this.getEntry();
    }

    public RoomTargetingSpell addItemToSummon(Item item) {
        this.itemsToSummon.add(item);
        return this;
    }

    public RoomTargetingSpell addItemToBanish(Item item) {
        this.itemsToBanish.add(item);
        return this;
    }

    public RoomTargetingSpell addCreatureToSummon(Creature creature) {
        this.creaturesToSummon.add(creature);
        return this;
    }

    public RoomTargetingSpell addCreatureToBanish(Creature creature) {
        // TODO: limit this number somehow
        this.creaturesToBanish.add(creature);
        return this;
    }

    public List<Item> getItemsToSummon() {
        return itemsToSummon;
    }

    public List<Item> getItemsToBanish() {
        return itemsToBanish;
    }

    public Set<Creature> getCreaturesToSummon() {
        return creaturesToSummon;
    }

    public Set<Creature> getCreaturesToBanish() {
        return creaturesToBanish;
    }

    @Override
    public boolean isOffensive() {
        if (this.getCreaturesToBanish().size() > 0) {
            return true;
        }
        return false;
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
