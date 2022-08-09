package com.lhf.game.magic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.EntityEffector.EffectPersistence;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.item.Item;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.SeeOutMessage;

public class RoomTargetingSpellEntry extends SpellEntry {
    private class NameAndCount {
        public String name;
        public int count;

        public NameAndCount(String name, int count) {
            if (count < 1) {
                count = 1;
            }
            this.name = name;
            this.count = count;
        }

    }

    protected Map<String, NameAndCount> itemClassNamesToSummon;
    protected Map<String, NameAndCount> creaturesToSummon;
    protected boolean banishesItems;
    protected boolean banishesCreatures;

    private void init() {
        this.itemClassNamesToSummon = new TreeMap<>();
        this.creaturesToSummon = new TreeMap<>();
    }

    public RoomTargetingSpellEntry(Integer level, String name, EffectPersistence persistence, String description,
            boolean banishesItems, boolean banishesCreatures) {
        super(level, name, persistence, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
    }

    public RoomTargetingSpellEntry(Integer level, String name, String description,
            boolean banishesItems, boolean banishesCreatures) {
        super(level, name, EffectPersistence.DURATION, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
    }

    public RoomTargetingSpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description, boolean banishesItems, boolean banishesCreatures) {
        super(level, name, invocation, persistence, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
    }

    public RoomTargetingSpellEntry(Integer level, String name, String invocation,
            String description, boolean banishesItems, boolean banishesCreatures) {
        super(level, name, invocation, EffectPersistence.DURATION, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
    }

    public RoomTargetingSpellEntry(RoomTargetingSpellEntry other) {
        super(other);
        this.banishesItems = other.banishesItems;
        this.banishesCreatures = other.banishesCreatures;
        this.creaturesToSummon = new TreeMap<>(other.creaturesToSummon);
        this.itemClassNamesToSummon = new TreeMap<>(other.itemClassNamesToSummon);
    }

    public RoomTargetingSpellEntry addSummonItem(Item item, int count) {
        this.itemClassNamesToSummon.put(item.getClassName(), new NameAndCount(item.getColorTaggedName(), count));
        return this;
    }

    private RoomTargetingSpellEntry addCreatureToSummon(String className, String name, int count) {
        this.creaturesToSummon.put(className, new NameAndCount(
                "<creature>" + name + "</creature>", count));
        return this;
    }

    public RoomTargetingSpellEntry addCreatureToSummon(Statblock statblock, int count) {
        return this.addCreatureToSummon(statblock.getCreatureRace(), statblock.getCreatureRace(), count);
    }

    public RoomTargetingSpellEntry addCreatureToSummon(Creature creature, int count) {
        return this.addCreatureToSummon(creature.getCreatureRace(), creature.getCreatureRace(), count);
    }

    public Map<String, NameAndCount> getItemClassNamesToSummon() {
        return itemClassNamesToSummon;
    }

    public Map<String, NameAndCount> getCreaturesToSummon() {
        return creaturesToSummon;
    }

    public boolean isBanishesItems() {
        return banishesItems;
    }

    public boolean isBanishesCreatures() {
        return banishesCreatures;
    }

    @Override
    public SeeOutMessage produceMessage() {
        return new SeeOutMessage(this);
    }

    @Override
    public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets) {
        return new CastingMessage(caster, this, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (this.itemClassNamesToSummon.size() > 0) {
            sb.append("This spell will summon the items: ");
            for (NameAndCount nameAndCount : this.itemClassNamesToSummon.values()) {
                sb.append(nameAndCount.count).append(" ").append(nameAndCount.name);
            }
            sb.append("\r\n");
        }
        if (this.creaturesToSummon.size() > 0) {
            sb.append("This spell will summon the creatures: ");
            for (NameAndCount nameAndCount : this.itemClassNamesToSummon.values()) {
                sb.append(nameAndCount.count).append(" ").append(nameAndCount.name);
            }
            sb.append("\r\n");
        }
        if (this.banishesCreatures) {
            sb.append("This spell will banish creatures. ").append("\r\n");
        }
        if (this.banishesItems) {
            sb.append("This spell will banish items. ").append("\r\n");
        }
        return sb.toString();
    }

}
