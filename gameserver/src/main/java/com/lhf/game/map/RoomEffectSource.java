package com.lhf.game.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.item.Item;

public class RoomEffectSource extends EntityEffectSource {

    protected List<Item> itemsToSummon;

    protected List<Item> itemsToBanish;

    protected Set<Creature> creaturesToSummon;

    protected Set<Creature> creaturesToBanish;

    public RoomEffectSource(String name, EffectPersistence persistence, String description) {
        super(name, persistence, description);
        this.itemsToSummon = new ArrayList<>();
        this.itemsToBanish = new ArrayList<>();
        this.creaturesToBanish = new HashSet<>();
        this.creaturesToSummon = new HashSet<>();
    }

    public RoomEffectSource addItemToSummon(Item toSummon) {
        this.itemsToSummon.add(toSummon);
        return this;
    }

    public RoomEffectSource addItemToBanish(Item toBanish) {
        this.itemsToBanish.add(toBanish);
        return this;
    }

    public RoomEffectSource addCreatureToSummon(Creature toSummon) {
        this.creaturesToSummon.add(toSummon);
        return this;
    }

    public RoomEffectSource addCreatureToBanish(Creature toBanish) {
        this.creaturesToBanish.add(toBanish);
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
        if (this.creaturesToBanish.size() > 0) {
            return true;
        }
        return false;
    }

    private void printCreatures(StringBuilder sb, Set<Creature> creatures, String action) {
        if (creatures.size() > 0) {
            sb.append("Creatures it will ").append(action).append(":\r\n");
            StringJoiner sj = new StringJoiner(", ");
            for (Creature creature : creatures) {
                sj.add(creature.getColorTaggedName());
            }
            sb.append(sj.toString()).append("\r\n");
        }
    }

    private void printItems(StringBuilder sb, List<Item> items, String action) {
        if (items.size() > 0) {
            sb.append("Items it will ").append(action).append(":\r\n");
            StringJoiner sj = new StringJoiner(", ");
            for (Item item : items) {
                sj.add(item.getColorTaggedName());
            }
            sb.append(sj.toString()).append("\r\n");
        }
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(super.printDescription());
        this.printCreatures(sb, this.creaturesToBanish, "banish");
        this.printCreatures(sb, this.creaturesToSummon, "summon");
        this.printItems(sb, this.itemsToBanish, "banish");
        this.printItems(sb, this.itemsToSummon, "summon");
        return sb.toString();
    }

}
