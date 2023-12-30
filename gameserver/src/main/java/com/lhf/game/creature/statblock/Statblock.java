package com.lhf.game.creature.statblock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class Statblock {

    // Reactions to DamageFlavors
    public static class DamageFlavorReactions {
        protected EnumSet<DamageFlavor> weaknesses;
        protected EnumSet<DamageFlavor> resistances;
        protected EnumSet<DamageFlavor> immunities;
        protected EnumSet<DamageFlavor> healing;

        private DamageFlavorReactions() {
            this.weaknesses = EnumSet.noneOf(DamageFlavor.class);
            this.resistances = EnumSet.noneOf(DamageFlavor.class);
            this.immunities = EnumSet.noneOf(DamageFlavor.class);
            this.healing = EnumSet.noneOf(DamageFlavor.class);
        }

        public static DamageFlavorReactions empty() {
            return new DamageFlavorReactions();
        }

        public static DamageFlavorReactions standard() {
            DamageFlavorReactions reactions = new DamageFlavorReactions();
            reactions.immunities.add(DamageFlavor.AGGRO);
            reactions.healing.add(DamageFlavor.HEALING);
            return reactions;
        }

        public static DamageFlavorReactions undead() {
            DamageFlavorReactions reactions = new DamageFlavorReactions();
            reactions.immunities.add(DamageFlavor.AGGRO);
            reactions.healing.remove(DamageFlavor.HEALING);
            reactions.healing.add(DamageFlavor.NECROTIC);
            reactions.weaknesses.add(DamageFlavor.HEALING);
            return reactions;
        }

        public static DamageFlavorReactions clone(DamageFlavorReactions other) {
            DamageFlavorReactions reactions = new DamageFlavorReactions();
            reactions.weaknesses = EnumSet.copyOf(other.weaknesses);
            reactions.resistances = EnumSet.copyOf(other.resistances);
            reactions.immunities = EnumSet.copyOf(other.immunities);
            reactions.healing = EnumSet.copyOf(other.healing);
            return reactions;
        }

        public Set<DamageFlavor> getWeaknesses() {
            return Collections.unmodifiableSet(weaknesses);
        }

        public Set<DamageFlavor> getResistances() {
            return Collections.unmodifiableSet(resistances);
        }

        public Set<DamageFlavor> getImmunities() {
            return Collections.unmodifiableSet(immunities);
        }

        public Set<DamageFlavor> getHealing() {
            return Collections.unmodifiableSet(healing);
        }

    }

    private String creatureRace;

    private AttributeBlock attributes;

    /** contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC */
    private Map<Stats, Integer> stats;
    /**
     * contains subtypes and items
     * // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
     * // and example item would be lightCrossbow
     */
    private Set<EquipmentTypes> proficiencies;

    // Inventory
    private Inventory inventory;
    // Equipment slots
    private Map<EquipmentSlots, Equipable> equipmentSlots;

    private DamageFlavorReactions damageFlavorReactions;

    public static final int DEFAULT_HP = 12;
    public static final int DEFAULT_AC = 11;
    public static final int DEFAULT_XP_WORTH = 500;

    public Statblock() {
        this.creatureRace = "creature";
        this.attributes = new AttributeBlock();
        this.stats = Collections.synchronizedMap(new EnumMap<>(Stats.class));
        // Set default stats
        this.stats.put(Stats.MAXHP, Statblock.DEFAULT_HP);
        this.stats.put(Stats.CURRENTHP, Statblock.DEFAULT_HP);
        this.stats.put(Stats.AC, Statblock.DEFAULT_AC);
        this.stats.put(Stats.XPWORTH, Statblock.DEFAULT_XP_WORTH);
        this.proficiencies = Collections.synchronizedSet(EnumSet.noneOf(EquipmentTypes.class));
        this.inventory = new Inventory();
        this.equipmentSlots = Collections.synchronizedMap(new EnumMap<>(EquipmentSlots.class));
        this.damageFlavorReactions = DamageFlavorReactions.standard();
    }

    public Statblock(String creatureRace) {
        this.creatureRace = creatureRace;
        this.attributes = new AttributeBlock();
        this.stats = Collections.synchronizedMap(new EnumMap<>(Stats.class));
        this.proficiencies = Collections.synchronizedSet(EnumSet.noneOf(EquipmentTypes.class));
        this.inventory = new Inventory();
        this.equipmentSlots = Collections.synchronizedMap(new EnumMap<>(EquipmentSlots.class));
        this.damageFlavorReactions = DamageFlavorReactions.standard();
    }

    public Statblock(String creatureRace, AttributeBlock attributes,
            EnumMap<Stats, Integer> stats,
            EnumSet<EquipmentTypes> proficiencies, Inventory inventory,
            EnumMap<EquipmentSlots, Equipable> equipmentSlots,
            DamageFlavorReactions damageFlavorReactions) {
        this.creatureRace = creatureRace;
        this.attributes = attributes;
        this.stats = Collections.synchronizedMap(stats.clone());
        this.proficiencies = Collections.synchronizedSet(proficiencies.clone());
        this.inventory = inventory;
        this.equipmentSlots = Collections.synchronizedMap(equipmentSlots.clone());
        this.damageFlavorReactions = damageFlavorReactions != null ? damageFlavorReactions
                : DamageFlavorReactions.standard();
    }

    public Statblock(Statblock other) {
        this.creatureRace = other.getCreatureRace();
        this.attributes = new AttributeBlock(other.attributes);
        this.stats = Collections.synchronizedMap(new EnumMap<>(other.stats));
        this.proficiencies = Collections.synchronizedSet(EnumSet.copyOf(other.proficiencies));
        this.inventory = new Inventory(other.inventory);
        this.equipmentSlots = Collections.synchronizedMap(new EnumMap<>(other.equipmentSlots));
        this.damageFlavorReactions = DamageFlavorReactions.clone(other.getDamageFlavorReactions());
    }

    @Override
    public String toString() {
        return creatureRace + "\n" +
                attributes.toString() + "\n" +
                stats.toString() + "\n" +
                proficiencies.toString() + "\n" +
                inventory.toStoreString() + "\n" +
                equipmentSlotsToString() + "\n";

    }

    public void setCreatureRace(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    public AttributeBlock getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeBlock attributes) {
        this.attributes = attributes;
    }

    public Map<Stats, Integer> getStats() {
        return stats;
    }

    public void setStats(EnumMap<Stats, Integer> stats) {
        this.stats = stats;
    }

    public Set<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(EnumSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Map<EquipmentSlots, Equipable> getEquipmentSlots() {
        return equipmentSlots;
    }

    public void setEquipmentSlots(EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
    }

    public String getCreatureRace() {
        return creatureRace;
    }

    public DamageFlavorReactions getDamageFlavorReactions() {
        if (this.damageFlavorReactions == null) {
            this.damageFlavorReactions = DamageFlavorReactions.standard();
            ;
        }
        return damageFlavorReactions;
    }

    public void setDamageFlavorReactions(DamageFlavorReactions damageFlavorReactions) {
        this.damageFlavorReactions = damageFlavorReactions != null ? damageFlavorReactions
                : DamageFlavorReactions.standard();
        ;
    }

    private String equipmentSlotsToString() {
        // EquipmentSlots[] slotValues = EquipmentSlots.values();
        StringBuilder stringBuilder = new StringBuilder("{");
        for (EquipmentSlots key : equipmentSlots.keySet()) {
            String item_name = equipmentSlots.get(key).getName();
            if (item_name == null) {
                item_name = "empty";
            }
            stringBuilder.append(key).append("=").append(item_name).append(",");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}