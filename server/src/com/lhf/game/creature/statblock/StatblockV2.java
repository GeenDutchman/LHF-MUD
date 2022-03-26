package com.lhf.game.creature.statblock;

import java.util.HashMap;
import java.util.HashSet;

import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.CreatureType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.Item;

public class StatblockV2 {
    protected String creatureRace;
    protected CreatureType creatureType;
    protected AttributeBlock abilityScores;
    protected Integer maxHealth;
    protected Integer baseArmorClass;
    protected Integer proficencyBonus;
    protected Inventory inventory;
    protected HashMap<EquipmentSlots, Item> equippedItems;
    protected HashSet<EquipmentTypes> proficiencies;

    public StatblockV2() {
    }

    public Integer getProficencyBonus() {
        return proficencyBonus;
    }

    public void setProficencyBonus(Integer proficencyBonus) {
        this.proficencyBonus = proficencyBonus;
    }

    public String getCreatureRace() {
        return creatureRace;
    }

    public void setCreatureRace(String creatureRace) {
        this.creatureRace = creatureRace;
    }

    public CreatureType getCreatureType() {
        return creatureType;
    }

    public void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public AttributeBlock getAbilityScores() {
        return abilityScores;
    }

    public void setAbilityScores(AttributeBlock abilityScores) {
        this.abilityScores = abilityScores;
    }

    public Integer getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(Integer maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getBaseArmorClass() {
        return baseArmorClass;
    }

    public void setBaseArmorClass(Integer baseArmorClass) {
        this.baseArmorClass = baseArmorClass;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public HashMap<EquipmentSlots, Item> getEquippedItems() {
        return equippedItems;
    }

    public void setEquippedItems(HashMap<EquipmentSlots, Item> equippedItems) {
        this.equippedItems = equippedItems;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public void setProficiencies(HashSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
    }

}
