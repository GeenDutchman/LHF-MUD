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

    //NOTE: if not used with a player this won't work
    public Statblock(Creature creature){
        if(creature.getCreatureType().equals(CreatureType.PLAYER)){
            this.name = creature.getName();
            this.creatureType = creature.getCreatureType();
            this.attributes = creature.getAttributes();
            this.modifiers = creature.getModifiers();
            this.stats = creature.getStats();
            this.proficiencies = creature.getProficiencies();
            this.inventory = creature.getInventory();
            this.equipmentSlots = creature.getEquipmentSlots();
        }

    }

    //may need to make this a file stream?
    public Statblock(String stringified_block){
        // Order is name, creatureType, attributes(map), modifiers(map),
        // stats(set), proficiencies(set), inventory, equipmentSlots(set)
        String[] block_lines = stringified_block.split("\n");

        // Name may need to change based on is a monster and how many need to be loaded
        this.name = block_lines[0];

        String creatureType = block_lines[1];
        if(creatureType.equalsIgnoreCase("player")){
            this.creatureType = CreatureType.PLAYER;
        }
        else if(creatureType.equalsIgnoreCase("monster")){
            this.creatureType = CreatureType.MONSTER;
        }
        else{
            this.creatureType = CreatureType.NPC;
        }

        this.attributes = attributesFromString(block_lines[2]);
        this.modifiers = attributesFromString(block_lines[3]);

        this.stats = statsFromString(block_lines[4]);
        this.proficiencies = proficienciesFromString(block_lines[5]);
        this.inventory = inventoryFromString(block_lines[6]);
        this.equipmentSlots = equipmentSlotsFromString(block_lines[7]);
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

    @Override
    public String toString(){
        return  name + "\n"+
                creatureType.toString()+"\n"+
                attributes.toString()+"\n"+
                modifiers.toString()+"\n"+
                stats.toString()+"\n"+
                proficiencies.toString()+"\n"+
                inventory.toString()+"\n"+
                equipmentSlots.toString()+"\n";

    }

    private HashMap<Attributes, Integer> attributesFromString(String line){
        line = line.substring(1,line.length()-1);
        String[] pairs = line.split(",");
        HashMap attributes = new HashMap();

        for(int i = 0; i< Attributes.values().length; i ++){
            String[] key_val = pairs[i].split("=");
            attributes.put(Attributes.valueOf(key_val[0].replace(" ","")),key_val[1]);
        }

        return attributes;
    }

    private HashMap<Stats,Integer> statsFromString(String line){
        HashMap stats = new HashMap();
        line = line.substring(1,line.length()-1);
        String[] pairs = line.split(",");

        for(int i = 0; i< Stats.values().length; i ++){
            String[] key_val = pairs[i].split("=");
            stats.put(Stats.valueOf(key_val[0].replace(" ","")),key_val[1]);
        }

        return stats;
    }

    private HashSet<EquipmentTypes> proficienciesFromString (String line){
        HashSet proficiencies = new HashSet();
        line = line.substring(1,line.length()-1);
        String [] proficiencies_strings = line.split(",");
        for(int i =0; i < proficiencies_strings.length; i++){
            String proficiency = proficiencies_strings[i].replace(" ","");
            if(proficiency.equalsIgnoreCase("")){continue;}
            proficiencies.add(EquipmentTypes.valueOf(proficiency));
        }

        return proficiencies;
    }

    private Inventory inventoryFromString(String line){
        Inventory inventory = new Inventory();

        return inventory;
    }

    private HashMap<EquipmentSlots, Item> equipmentSlotsFromString(String line){
        HashMap equipSlots = new HashMap();

        /*
        line = line.substring(1,line.length()-1);
        String[] pairs = line.split(",");

        for(int i = 0; i< EquipmentSlots.values().length; i ++){
            String[] key_val = pairs[i].split("=");
            equipSlots.put(EquipmentSlots.values()[i],key_val[1]);
        }
        */

        return equipSlots;
    }

}