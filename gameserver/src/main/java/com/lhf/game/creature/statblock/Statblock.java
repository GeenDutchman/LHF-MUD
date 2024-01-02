package com.lhf.game.creature.statblock;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;

public class Statblock {

    // Reactions to DamageFlavors
    public enum DamgeFlavorReaction {
        WEAKNESSES,
        RESISTANCES,
        IMMUNITIES,
        CURATIVES
    };

    private String creatureRace;

    private AttributeBlock attributes;

    /** contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC */
    private EnumMap<Stats, Integer> stats;
    /**
     * contains subtypes and items
     * // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
     * // and example item would be lightCrossbow
     */
    private EnumSet<EquipmentTypes> proficiencies;

    // Inventory
    private Inventory inventory;
    // Equipment slots
    private EnumMap<EquipmentSlots, Equipable> equipmentSlots;

    private EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> damageFlavorReactions;

    public static final int DEFAULT_HP = 12;
    public static final int DEFAULT_AC = 11;
    public static final int DEFAULT_XP_WORTH = 500;

    public static class StatblockBuilder implements Serializable {
        private String creatureRace;
        private AttributeBlock attributeBlock;
        private EnumMap<Stats, Integer> stats;
        private EnumSet<EquipmentTypes> proficiencies;
        private Inventory inventory;
        private EnumMap<EquipmentSlots, Equipable> equipmentSlots;
        private EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> damageFlavorReactions;

        public StatblockBuilder() {
            this.creatureRace = "Creature";
            this.attributeBlock = new AttributeBlock();
            this.stats = new EnumMap<>(Stats.class);
            this.defaultStats();
            this.proficiencies = EnumSet.noneOf(EquipmentTypes.class);
            this.inventory = new Inventory();
            this.equipmentSlots = new EnumMap<>(EquipmentSlots.class);
            this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
            this.defaultFlavorReactions();
        }

        public StatblockBuilder setCreatureRace(String race) {
            this.creatureRace = race != null && !race.isBlank() ? race : "Creature";
            return this;
        }

        public StatblockBuilder setAttributeBlock(AttributeBlock other) {
            this.attributeBlock = other != null ? other : new AttributeBlock();
            return this;
        }

        public StatblockBuilder setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
                Integer intelligence,
                Integer wisdom, Integer charisma) {
            this.attributeBlock = new AttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
            return this;
        }

        public StatblockBuilder defaultStats() {
            this.stats.put(Stats.MAXHP, Statblock.DEFAULT_HP);
            this.stats.put(Stats.CURRENTHP, Statblock.DEFAULT_HP);
            this.stats.put(Stats.AC, Statblock.DEFAULT_AC);
            this.stats.put(Stats.XPWORTH, Statblock.DEFAULT_XP_WORTH);
            return this;
        }

        public StatblockBuilder setStat(Stats stat, int value) {
            if (stat != null) {
                this.stats.put(stat, value);
            }
            return this;
        }

        public StatblockBuilder resetProficiencies() {
            this.proficiencies.clear();
            return this;
        }

        public StatblockBuilder addProficiency(EquipmentTypes equipType) {
            if (equipType != null) {
                this.proficiencies.add(equipType);
            }
            return this;
        }

        public StatblockBuilder addProficiencies(Set<EquipmentTypes> eqiupTypes) {
            if (eqiupTypes != null) {
                this.proficiencies.addAll(eqiupTypes);
            }
            return this;
        }

        public StatblockBuilder setInventory(Inventory other) {
            if (other != null) {
                this.inventory = new Inventory();
                ItemContainer.transfer(other, this.inventory, null, true);
            }
            return this;
        }

        public StatblockBuilder addItemToInventory(Takeable takeable) {
            if (takeable != null) {
                this.inventory.addItem(takeable);
            }
            return this;
        }

        public StatblockBuilder resetEquipment() {
            this.equipmentSlots.clear();
            return this;
        }

        public StatblockBuilder addEquipment(EquipmentSlots slot, Equipable equipable) {
            if (slot != null && equipable != null) {
                this.addItemToInventory(this.equipmentSlots.put(slot, equipable));
            }
            return this;
        }

        public static void setDefaultFlavorReactions(
                EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> needDefaults) {
            if (needDefaults == null) {
                needDefaults = new EnumMap<>(DamgeFlavorReaction.class);
            }
            needDefaults
                    .computeIfAbsent(DamgeFlavorReaction.CURATIVES, key -> EnumSet.of(DamageFlavor.HEALING))
                    .add(DamageFlavor.HEALING);
            needDefaults
                    .computeIfAbsent(DamgeFlavorReaction.IMMUNITIES, key -> EnumSet.of(DamageFlavor.AGGRO))
                    .add(DamageFlavor.AGGRO);
        }

        public StatblockBuilder defaultFlavorReactions() {
            StatblockBuilder.setDefaultFlavorReactions(this.damageFlavorReactions);
            return this;
        }

        public StatblockBuilder resetFlavorReactions() {
            this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
            return this;
        }

        public StatblockBuilder addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
            if (sort != null && flavor != null) {
                this.damageFlavorReactions.computeIfAbsent(sort, key -> EnumSet.of(flavor)).add(flavor);
            }
            return this;
        }

        public String getCreatureRace() {
            return creatureRace;
        }

        public AttributeBlock getAttributeBlock() {
            return new AttributeBlock(attributeBlock);
        }

        public EnumMap<Stats, Integer> getStats() {
            return new EnumMap<>(this.stats);
        }

        public EnumSet<EquipmentTypes> getProficiencies() {
            return EnumSet.copyOf(proficiencies);
        }

        public Inventory getInventory() {
            return new Inventory(inventory);
        }

        public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
            return new EnumMap<>(this.equipmentSlots); // this is a shallow copy of the equipables, but whatever
        }

        public static EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> deepCloneDamageFlavorReactions(
                EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> from) {
            EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> reactions = new EnumMap<>(
                    DamgeFlavorReaction.class);
            if (from != null) {
                for (final Entry<DamgeFlavorReaction, EnumSet<DamageFlavor>> entry : from.entrySet()) {
                    reactions.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
                }
            }
            return reactions;
        }

        public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
            return StatblockBuilder.deepCloneDamageFlavorReactions(this.damageFlavorReactions);
        }

        public Statblock build() {
            return new Statblock(this);
        }

    }

    public static StatblockBuilder getBuilder() {
        return new StatblockBuilder();
    }

    private Statblock(StatblockBuilder builder) {
        this.creatureRace = builder.getCreatureRace();
        this.attributes = builder.getAttributeBlock();
        this.stats = builder.getStats();
        this.proficiencies = builder.getProficiencies();
        this.inventory = builder.getInventory();
        this.equipmentSlots = builder.getEquipmentSlots();
        this.damageFlavorReactions = builder.getDamageFlavorReactions();
    }

    public Statblock(String creatureRace, AttributeBlock attributes,
            EnumMap<Stats, Integer> stats,
            EnumSet<EquipmentTypes> proficiencies, Inventory inventory,
            EnumMap<EquipmentSlots, Equipable> equipmentSlots,
            EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> reactions) {
        this.creatureRace = creatureRace;
        this.attributes = attributes;
        this.stats = stats.clone();
        this.proficiencies = proficiencies.clone();
        this.inventory = inventory;
        this.equipmentSlots = equipmentSlots.clone();
        if (reactions == null) {
            this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
            StatblockBuilder.setDefaultFlavorReactions(this.damageFlavorReactions);
        } else {
            this.damageFlavorReactions = reactions;
        }
    }

    public Statblock(Statblock other) {
        this.creatureRace = other.getCreatureRace();
        this.attributes = new AttributeBlock(other.attributes);
        this.stats = new EnumMap<>(other.stats);
        this.proficiencies = EnumSet.copyOf(other.proficiencies);
        this.inventory = new Inventory(other.inventory);
        this.equipmentSlots = new EnumMap<>(other.equipmentSlots);
        this.damageFlavorReactions = StatblockBuilder.deepCloneDamageFlavorReactions(other.damageFlavorReactions);
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

    public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
        return damageFlavorReactions;
    }

    public void setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> reactions) {
        if (reactions == null) {
            StatblockBuilder.setDefaultFlavorReactions(reactions);
        }
        this.damageFlavorReactions = reactions;
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