package com.lhf.game.creature;

import com.lhf.game.Attack;
import com.lhf.game.inventory.EquipmentOwner;
import com.lhf.game.inventory.Inventory;
import com.lhf.game.inventory.InventoryOwner;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.*;
import com.lhf.game.map.objects.roomobject.Corpse;
import com.lhf.game.shared.dice.Dice;
import com.lhf.game.shared.enums.*;
import javafx.util.Pair;

import java.util.*;

import static com.lhf.game.shared.enums.Attributes.*;

public class Creature implements InventoryOwner, EquipmentOwner {

    public class Fist extends Weapon {

        public Fist() {
            super("Fist", false);
        }

        @Override
        public int rollToHit() {
            return Dice.getInstance().d20(1);
        }

        @Override
        public int rollDamage() {
            return Dice.getInstance().d2(1);
        }

        @Override
        public Attack rollAttack() {
            return new Attack(this.rollToHit()).addFlavorAndDamage("Bludgeoning", this.rollDamage());
        }

        @Override
        public List<EquipmentTypes> getType() {
            List<EquipmentTypes> result = new ArrayList<>();
            result.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
            result.add(EquipmentTypes.MONSTERPART);
            return result;
        }

        @Override
        public List<EquipmentSlots> getWhichSlots() {
            List<EquipmentSlots> result = new ArrayList<>();
            result.add(EquipmentSlots.WEAPON);
            return result;
        }

        @Override
        public List<Pair<String, Integer>> equip() {
            return new ArrayList<>(0); // changes nothing
        }

        @Override
        public List<Pair<String, Integer>> unequip() {
            return new ArrayList<>(0); // changes nothing
        }

        @Override
        public String getDescription() {
            return "This is a " + getName() + " attached to a " + Creature.this.getName();
        }
    }

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
    public Creature(){
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
            current = 0;
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

    public void updateStat(Stats stat, int value) {
        this.stats.put(stat, this.stats.get(stat) + value);
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

    public Attack attack(String itemName, String target) {
        System.out.println(name + " is attempting to attack: " + target);
        Weapon toUse;
        Optional<Item> item = this.fromAllInventory(itemName);
        if (item.isPresent() && item.get() instanceof Weapon) {
            toUse = (Weapon) item.get();
        } else {
            toUse = this.getWeapon();
        }
        return toUse.rollAttack();
    }

    public Weapon getWeapon() {
        Weapon weapon = (Weapon) getWhatInSlot(EquipmentSlots.WEAPON);
        if (weapon == null) {
            return new Creature.Fist();
        } else {
            return weapon;
        }
    }

    public String applyAttack(Attack attack) {
        //add stuff to calculate if the attack hits or not, and return false if so
        StringBuilder output = new StringBuilder();
        Iterator attackIt = attack.iterator();
        while (attackIt.hasNext()) {
            Map.Entry entry = (Map.Entry) attackIt.next();
            String flavor = (String)entry.getKey();
            Integer damage = (Integer)entry.getValue();
            updateHitpoints(-damage);
            output.append(name + " has been dealt " + damage + " " + flavor + " damage.\n");
            if (stats.get(Stats.CURRENTHP) <= 0) {
                output.append(name + " has died.\n");
                break;
            }
        }
        return output.toString();
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

    @Override
    public Optional<Takeable> dropItem(String itemName) {
        Optional<Takeable> item = this.inventory.getItem(itemName);
        if (item.isPresent()) {
            this.inventory.removeItem(item.get());
            return item;
        }

        for (EquipmentSlots slot : this.equipmentSlots.keySet()) {
            Takeable thing = (Takeable) this.equipmentSlots.get(slot);
            if (thing.getName().equals(itemName)) {
                this.equipmentSlots.remove(slot);
                return Optional.of(thing);
            }
        }
        return Optional.empty();
    }

    @Override
    public void takeItem(Takeable item) {
        this.inventory.addItem(item);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public String listInventory() {
        StringBuilder sb = new StringBuilder();
        if (this.inventory.isEmpty()) {
            sb.append("Your inventory is empty.");
        } else {
            sb.append(this.inventory.toString());
        }
        sb.append("\n\r");

        for (EquipmentSlots slot : EquipmentSlots.values()) {
            Item item = this.equipmentSlots.get(slot);

            if (item == null) {
                sb.append(slot.toString()).append(": ").append("empty. ");
            } else {
                sb.append(slot.toString()).append(": ").append(item.getName()).append(". ");
            }
        }

        return sb.toString();
    }

    private Optional<Item> fromAllInventory(String itemName) {
        Optional<Takeable> maybeTakeable = this.inventory.getItem(itemName);
        if (maybeTakeable.isPresent()) {
            return Optional.of((Item) maybeTakeable.get());
        }

        for (Item equipped : this.equipmentSlots.values()) {
            if (equipped.getName().equals(itemName)) {
                return Optional.of((equipped));
            }
        }

        return Optional.empty();
    }

    @Override
    public String useItem(String itemName, Object onWhat) {
        // if onWhat is not specified, use this creature
        Object useOn = onWhat;
        if (useOn == null) {
            useOn = this;
        }

        Optional<Item> maybeItem = this.fromAllInventory(itemName);
        if (maybeItem.isPresent()) {
            Item item = maybeItem.get();
            if (item instanceof Usable) {
                if (item instanceof Consumable && !((Usable) item).hasUsesLeft()) {
                    inventory.removeItem((Takeable) item);
                }
                return ((Usable) item).doUseAction(useOn);
            }
            return itemName + " is not usable!";
        }
        return "You do not have that " + itemName + " to use!";
    }

    private boolean applyUse(List<Pair<String, Integer>> applications) {
        for (Pair<String, Integer> p : applications) {
            try {
                Attributes attribute = Attributes.valueOf(p.getKey());
                this.updateAttribute(attribute, p.getValue());
            } catch (IllegalArgumentException e) {
                try {
                    Stats stat = Stats.valueOf(p.getKey());
                    this.updateStat(stat, p.getValue());
                } catch (IllegalArgumentException e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String equipItem(String itemName, EquipmentSlots slot) {
        if (slot == null) {
            return "That is not a slot.  These are your options: " + Arrays.toString(EquipmentSlots.values()) + "\n\r";
        }
        if (this.inventory.hasItem(itemName)) {
            Optional<Takeable> item = this.inventory.getItem(itemName);
            if (item.get() instanceof Equipable) {
                Equipable thing = (Equipable) item.get();
                if (thing.getWhichSlots().contains(slot)) {
                    String unequipMessage = this.unequipItem(slot);
                    this.applyUse(thing.equip());
                    this.inventory.removeItem(thing);
                    this.equipmentSlots.putIfAbsent(slot, (Item) thing);
                    return unequipMessage + thing.getName() + " successfully equipped!\n\r";
                }
                return "You cannot equip the " + thing.getName() + " to " + slot.toString() + "\n\r";
            }
            return itemName + " is not equippable!\n\r";
        }

        return itemName + " is not in your inventory, so you cannot equip it!\n\r";
    }

    @Override
    public String unequipItem(EquipmentSlots slot) {
        if (slot == null) {
            return "That is not a slot.  These are your options: " + Arrays.toString(EquipmentSlots.values()) + "\n\r";
        }
        Equipable thing = (Equipable) this.equipmentSlots.remove(slot);
        if (thing != null) {
            this.applyUse(thing.unequip());
            this.inventory.addItem(thing);
            return "You have unequipped your " + thing.getName() + "\n\r";
        }
        return "That slot is empty.\n\r";
    }

    public Corpse generateCorpseFromCreature() {
        //Make the corpse if called
        return null;
    }
}