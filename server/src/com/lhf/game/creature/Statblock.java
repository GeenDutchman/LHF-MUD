package com.lhf.game.creature;

import com.lhf.game.inventory.Inventory;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.shared.enums.*;

import java.util.HashMap;
import java.util.HashSet;

public class Statblock {

    public Statblock(String name, CreatureType creatureType, HashMap<Attributes, Integer> attributes,
                     HashMap<Attributes, Integer> modifiers, HashMap<Stats, Integer> stats,
                     HashSet<EquipmentTypes> proficiencies, Inventory inventory, HashMap<EquipmentSlots,
            Item> equipmentSlots) {
        this.name = name;
        this.creatureType = creatureType;
        this.attributes = attributes;
        this.modifiers = modifiers;
        this.stats = stats;
        this.proficiencies = proficiencies;
        this.inventory = inventory;
        this.equipmentSlots = equipmentSlots;
    }

    // examples include a players username or goblin1 ect.
    public String name;
    // see the enums file
    public CreatureType creatureType;

    //public MonsterType monsterType;// I dont know if we'll need this

    // attributes and modifiers must in the following order:
    // STR:0, DEX:1, CON:2 , INT:3, WIS:4, CHA:5
    public HashMap<Attributes, Integer> attributes;
    public HashMap<Attributes, Integer> modifiers;
    //contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC
    public HashMap<Stats, Integer> stats;
    // contains subtypes and items
    // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
    // and example item would be lightCrossbow
    public HashSet<EquipmentTypes> proficiencies;
    /* TODO: or to add once things are done
     Abilities... initial thought was some kind of array
    */

    //Inventory
    public Inventory inventory;
    //Equipment slots
    public HashMap<EquipmentSlots, Item> equipmentSlots;

    //TODO: add a statblock class mapped by creature type or username containing:
    // creatureType,attributes, modifiers, stats, proficiencies, inventory, and
    // an array of Items called equipped
    // I think this would allow us to store a logged off user
    // and generic stats for creatures
    public Statblock(String name, CreatureType creatureType, HashMap<Attributes, Integer> attributes,
                     HashMap<Attributes, Integer> modifiers, HashMap<Stats, Integer> stats,
                     HashSet<EquipmentTypes> proficiencies, HashMap<EquipmentSlots, Item> equipmentSlots) {
        this.name = name;

        this.creatureType = creatureType;
        this.attributes = attributes;
        this.modifiers = modifiers;
        this.stats = stats;
        this.proficiencies = proficiencies;
        // add abilities if we get to it
        // this.inventory = inventory //uncomment when Inventory class is ready
        this.equipmentSlots = equipmentSlots;
    }
}