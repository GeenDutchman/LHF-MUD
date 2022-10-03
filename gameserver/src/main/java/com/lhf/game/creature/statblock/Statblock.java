package com.lhf.game.creature.statblock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class Statblock {

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
    /*
     * TODO: or to add once things are done
     * Abilities... initial thought was some kind of array
     */

    // Inventory
    private Inventory inventory;
    // Equipment slots
    private Map<EquipmentSlots, Equipable> equipmentSlots;

    public Statblock() {
        this.creatureRace = "creature";
        this.attributes = new AttributeBlock();
        this.stats = Collections.synchronizedMap(new EnumMap<>(Stats.class));
        // Set default stats
        this.stats.put(Stats.MAXHP, 12);
        this.stats.put(Stats.CURRENTHP, 12);
        this.stats.put(Stats.AC, 11);
        this.stats.put(Stats.XPWORTH, 500);
        this.proficiencies = Collections.synchronizedSet(EnumSet.noneOf(EquipmentTypes.class));
        this.inventory = new Inventory();
        this.equipmentSlots = Collections.synchronizedMap(new EnumMap<>(EquipmentSlots.class));
    }

    public Statblock(String creatureRace) {
        this.creatureRace = creatureRace;
        this.attributes = new AttributeBlock();
        this.stats = Collections.synchronizedMap(new EnumMap<>(Stats.class));
        this.proficiencies = Collections.synchronizedSet(EnumSet.noneOf(EquipmentTypes.class));
        this.inventory = new Inventory();
        this.equipmentSlots = Collections.synchronizedMap(new EnumMap<>(EquipmentSlots.class));
    }

    public Statblock(String creatureRace, AttributeBlock attributes,
            EnumMap<Stats, Integer> stats,
            EnumSet<EquipmentTypes> proficiencies, Inventory inventory,
            EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        this.creatureRace = creatureRace;
        this.attributes = attributes;
        this.stats = Collections.synchronizedMap(stats.clone());
        this.proficiencies = Collections.synchronizedSet(proficiencies.clone());
        this.inventory = inventory;
        this.equipmentSlots = Collections.synchronizedMap(equipmentSlots.clone());
    }

    public Statblock(Statblock other) {
        this.creatureRace = other.getCreatureRace();
        this.attributes = other.getAttributes();
        this.stats = other.getStats();
        this.proficiencies = other.getProficiencies();
        this.inventory = other.getInventory();
        this.equipmentSlots = other.getEquipmentSlots();
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