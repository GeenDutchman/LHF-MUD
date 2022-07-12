package com.lhf.game.creature.statblock;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.*;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Equipable;

import java.util.HashMap;
import java.util.HashSet;

public class Statblock {

    public Statblock(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    public Statblock(String creatureRace, CreatureFaction faction, AttributeBlock attributes,
            HashMap<Stats, Integer> stats,
            HashSet<EquipmentTypes> proficiencies, Inventory inventory,
            HashMap<EquipmentSlots, Equipable> equipmentSlots) {
        this.creatureRace = creatureRace;
        this.faction = faction;
        this.attributes = attributes;
        this.stats = stats;
        this.proficiencies = proficiencies;
        this.inventory = inventory;
        this.equipmentSlots = equipmentSlots;
    }

    public Statblock(Creature creature) {
        this.creatureRace = creature.getCreatureRace();
        this.faction = creature.getFaction();
        this.attributes = creature.getAttributes();
        this.stats = creature.getStats();
        this.proficiencies = creature.getProficiencies();
        this.inventory = creature.getInventory();
        this.equipmentSlots = creature.getEquipmentSlots();

    }

    public String creatureRace;
    // see the enums file
    public CreatureFaction faction;

    public AttributeBlock attributes;
    // contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC
    public HashMap<Stats, Integer> stats;
    // contains subtypes and items
    // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
    // and example item would be lightCrossbow
    public HashSet<EquipmentTypes> proficiencies;
    /*
     * TODO: or to add once things are done
     * Abilities... initial thought was some kind of array
     */

    // Inventory
    public Inventory inventory;
    // Equipment slots
    public HashMap<EquipmentSlots, Equipable> equipmentSlots;

    public Statblock() {
    }

    @Override
    public String toString() {
        return creatureRace + "\n" +
                faction.toString() + "\n" +
                attributes.toString() + "\n" +
                stats.toString() + "\n" +
                proficiencies.toString() + "\n" +
                inventory.toStoreString() + "\n" +
                equipmentSlotsToString() + "\n";

    }

    public void setCreatureRace(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    public CreatureFaction getFaction() {
        return faction;
    }

    public void setFaction(CreatureFaction faction) {
        this.faction = faction;
    }

    public AttributeBlock getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributeBlock attributes) {
        this.attributes = attributes;
    }

    public HashMap<Stats, Integer> getStats() {
        return stats;
    }

    public void setStats(HashMap<Stats, Integer> stats) {
        this.stats = stats;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(HashSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public HashMap<EquipmentSlots, Equipable> getEquipmentSlots() {
        return equipmentSlots;
    }

    public void setEquipmentSlots(HashMap<EquipmentSlots, Equipable> equipmentSlots) {
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