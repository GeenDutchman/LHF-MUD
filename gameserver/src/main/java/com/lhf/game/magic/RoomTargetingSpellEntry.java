package com.lhf.game.magic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.events.messages.out.CastingMessage;
import com.lhf.game.item.Item;
import com.lhf.game.map.RoomEffectSource;

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
    protected final boolean banishesItems;
    protected final boolean banishesCreatures;

    private void init() {
        this.itemClassNamesToSummon = new TreeMap<>();
        this.creaturesToSummon = new TreeMap<>();
    }

    public RoomTargetingSpellEntry(ResourceCost level, String name, String invocation,
            Set<? extends RoomEffectSource> effectSources,
            Set<VocationName> allowed, String description,
            boolean banishesItems, boolean banishesCreatures) {
        super(level, name, invocation, effectSources, allowed, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
    }

    public RoomTargetingSpellEntry(ResourceCost level, String name, Set<? extends RoomEffectSource> effectSources,
            Set<VocationName> allowed, String description,
            boolean banishesItems, boolean banishesCreatures) {
        super(level, name, effectSources, allowed, description);
        this.banishesItems = banishesItems;
        this.banishesCreatures = banishesCreatures;
        this.init();
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
    public CastingMessage Cast(Creature caster, ResourceCost castLevel, List<? extends Taggable> targets) {
        StringJoiner sj = new StringJoiner(", ", "Targeting: ", "").setEmptyValue("nothing");
        if (targets != null) {
            for (Taggable taggable : targets) {
                sj.add(taggable.getColorTaggedName());
            }
        }
        return CastingMessage.getBuilder().setCaster(caster).setSpellEntry(this).setCastEffects(sj.toString()).Build();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(this.description);
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
        return sb.toString() + super.printEffectDescriptions();
    }

}
