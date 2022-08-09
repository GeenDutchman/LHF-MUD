package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;
import com.lhf.game.map.RoomEffector;

public class RoomTargetingSpell extends ISpell implements RoomEffector {
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

    public RoomTargetingSpell(RoomTargetingSpellEntry entry) {
        super(entry);
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

}
