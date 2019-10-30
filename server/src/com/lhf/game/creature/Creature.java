package com.lhf.game.creature;

import com.lhf.game.inventory.EquipmentOwner;
import com.lhf.game.inventory.Inventory;
import com.lhf.game.inventory.InventoryOwner;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.Takeable;
import com.lhf.game.shared.enums.*;

import java.util.HashMap;
import java.util.HashSet;

import static com.lhf.game.shared.enums.Attributes.*;

public class Creature implements InventoryOwner, EquipmentOwner {
    private String name; //Username for players, description name (e.g., goblin 1) for monsters/NPCs
    private CreatureType creatureType; //See shared enum
    //private MonsterType monsterType; // I dont know if we'll need this

    // attributes and modifiers must in the following order:
    // STR:0, DEX:1, CON:2 , INT:3, WIS:4, CHA:5
    private HashMap<Attributes, Integer> attributes;
    private HashMap<Attributes, Integer> modifiers;

    private HashMap<Stats, Integer> stats; //contains CurrentHp, MaxHp, Xp, proficiencyBonus, AC

    // contains subtypes and items
    // an example subtype would be MARTIAL_WEAPONS or HEAVY_ARMOR
    // and example item would be lightCrossbow
    private HashSet<EquipmentTypes> proficiencies;

    /* TODO: or to add once things are done
     Abilities... initial thought was some kind of array
    */
    private Inventory inventory; //This creature's inventory
    private HashMap<EquipmentSlots, Item> equipmentSlots; //See enum for slots

    private boolean inBattle; // Boolean to determine if this creature is in combat

    //Default constructor
    public Creature() {
        //Instantiate creature with no name and type Monster
        this.name = "";
        this.creatureType = CreatureType.MONSTER;

        //Set attributes to default values
        this.attributes = new HashMap<>();
        this.attributes.put(STR, 10);
        this.attributes.put(DEX, 10);
        this.attributes.put(CON, 10);
        this.attributes.put(INT, 10);
        this.attributes.put(WIS, 10);
        this.attributes.put(CHA, 10);

        //Set modifiers to default values
        this.modifiers = new HashMap<>();
        this.modifiers.put(STR, 0);
        this.modifiers.put(DEX, 0);
        this.modifiers.put(CON, 0);
        this.modifiers.put(INT, 0);
        this.modifiers.put(WIS, 0);
        this.modifiers.put(CHA, 0);

        //Set default stats (10 HP, 2 proficiency bonus, etc.)
        this.stats = new HashMap<>();
        this.stats.put(Stats.MAXHP, 10);
        this.stats.put(Stats.CURRENTHP, 10);
        this.stats.put(Stats.AC, 10);
        this.stats.put(Stats.PROFICIENCYBONUS, 2);
        this.stats.put(Stats.XPEARNED, 0);
        this.stats.put(Stats.XPWORTH, 100);

        //Set empty inventory
        this.inventory = new Inventory();

        //Set empty equip slots
        this.equipmentSlots = new HashMap<>();

        //Set empty proficiencies
        this.proficiencies = new HashSet<>();

        //We don't start them in battle
        this.inBattle = false;
    }

    //Statblock-based constructor
    public Creature(String name, Statblock statblock) {
        this.name = name;

        this.creatureType = statblock.creatureType;
        this.attributes = statblock.attributes;
        this.modifiers = statblock.modifiers;
        this.stats = statblock.stats;
        this.proficiencies = statblock.proficiencies;
        // add abilities if we get to it
        this.inventory = statblock.inventory; //uncomment when Inventory class is ready
        this.equipmentSlots = statblock.equipmentSlots;
    }

    public void updateHitpoints(int value) {
        int current = stats.get(Stats.CURRENTHP);
        int max = stats.get(Stats.MAXHP);
        current += value;
        if (current <= 0) {
            this.die();
        }
        if (current > max) {
            current = max;
        }
        stats.replace(Stats.CURRENTHP, current);
    }

    public void updateAc(int value) {
        int current = stats.get(Stats.AC);
        current += value;
        stats.replace(Stats.AC, current);
    }

    public void updateXp(int value) {
        int current = stats.get(Stats.XPEARNED);
        current += value;
        stats.replace(Stats.XPEARNED, current);
        if (this.canLevelUp(current, current - value)) {
            //this.levelUp();
        }
    }

    public void updateModifier(Attributes modifier, int value) {
        this.modifiers.put(modifier, this.modifiers.get(modifier) + value);
    }

    public void updateAttribute(Attributes attribute, int value) {
        this.attributes.put(attribute, this.attributes.get(attribute) + value);
    }



    /* start getters*/

    public HashMap<Stats, Integer> getStats() {
        return stats;
    }

    public String getName() {
        return name;
    }

    public CreatureType getCreatureType() {
        return creatureType;
    }

    public HashMap<Attributes, Integer> getAttributes() {
        return attributes;
    }

    public HashMap<Attributes, Integer> getModifiers() {
        return modifiers;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public HashMap<EquipmentSlots, Item> getEquipmentSlots() {
        return equipmentSlots;
    }

    public boolean isInBattle() {
        return this.inBattle;
    }

    public void attack(String itemName, String target) {
        System.out.println(name + " is attempting to attack: " + target);
    }

    //public void ( Ability ability, String target);

    private int getAttribute(Attributes attribute) {
        return this.attributes.getOrDefault(attribute, 10);
    }

    private Item getWhatInSlot(EquipmentSlots slot) {
        return this.equipmentSlots.get(slot);
    }

    //Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCreatureType(CreatureType creatureType) {
        this.creatureType = creatureType;
    }

    public void setAttributes(HashMap<Attributes, Integer> attributes) {
        this.attributes = attributes;
    }

    public void setModifiers(HashMap<Attributes, Integer> modifiers) {
        this.modifiers = modifiers;
    }

    public void setStats(HashMap<Stats, Integer> stats) {
        this.stats = stats;
    }

    public void setProficiencies(HashSet<EquipmentTypes> proficiencies) {
        this.proficiencies = proficiencies;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setEquipmentSlots(HashMap<EquipmentSlots, Item> equipmentSlots) {
        this.equipmentSlots = equipmentSlots;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    private boolean canLevelUp(int current, int former) {
        // if former is below threshold and current is above or equal.. do things
        // in normal 5e this is where we would add abilities and ASI
        // probablly we would pull them into a pocket dimension and explain
        // what leveling up means for them, allowing them to ASI and
        // any other relevant choices they need to make
        return false;
    }

    private void die() {
        System.out.println(name + "died");
        //should unequip all my stuff and put it into my inventory?
        //could also turn me into a room object called body that
        // might esentially be a chest with my inventory and equiped
        // items dropped into it
        // we would probally want to remove that from the room after a certain
        // amount of time
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Creature)) {
            return false;
        }
        Creature c = (Creature) obj;
        return c.getName().equals(getName());
    }

    /*public void drop(String itemName){
        if(inventory.find(itemName)){
            inventory.remove(item);
        }
    }*/

    @Override
    public void takeItem(Takeable item) {
        this.inventory.addItem(item);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void useItem(int itemIndex) {
        //Code here
        //System.out.println( name + " is attempting to use item: " + itemName);
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        /*
        if(inventory.find(itemName)){
            Item item = inventory.remove(itemName);
        }
        if(item.isEquipable()){
            index = getSlotIndex(item.getType);
            Item formerlyEquiped = equipmentSlots[index];
            equipmentSlots[index] = item;
            item.equip(); //im imagining this returns a modifer or AC to update and its value
            //call appropriate update function

            if(formerlyEquiped != null){
                unequip(formerlyEquiped);
            }
            System.out.println(name + " equiped: " + itemName);

        }
         */
        return true;
    }

    @Override
    public void unequipItem(EquipmentSlots slot) {
        //Code here
        //item.unequip(); //im imagining this returns a modifer or AC to update and its value
        // call appropriate update function
        //System.out.println(name + " unequiped: " + item.name);
    }
}