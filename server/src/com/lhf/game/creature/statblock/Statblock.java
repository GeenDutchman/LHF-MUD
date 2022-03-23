package com.lhf.game.creature.statblock;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.*;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;

public class Statblock {

    public Statblock(String name, CreatureType creatureType, AttributeBlock attributes,
            HashMap<Stats, Integer> stats,
            HashSet<EquipmentTypes> proficiencies, Inventory inventory, HashMap<EquipmentSlots, Item> equipmentSlots) {
        this.name = name;
        this.creatureType = creatureType;
        this.attributes = attributes;
        this.stats = stats;
        this.proficiencies = proficiencies;
        this.inventory = inventory;
        this.equipmentSlots = equipmentSlots;
    }

    public Statblock(Creature creature) {
        this.name = creature.getName();
        this.creatureType = creature.getCreatureType();
        this.attributes = creature.getAttributes();
        this.stats = creature.getStats();
        this.proficiencies = creature.getProficiencies();
        this.inventory = creature.getInventory();
        this.equipmentSlots = creature.getEquipmentSlots();

    }

    // may need to make this a file stream?
    public Statblock(String stringified_block) {
        // Order is name, creatureType, attributes(map), modifiers(map),
        // stats(set), proficiencies(set), inventory, equipmentSlots(set)
        String[] block_lines = stringified_block.split("\n");

        // Name may need to change based on is a monster and how many need to be loaded
        this.name = block_lines[0];

        String creatureType = block_lines[1];
        if (creatureType.equalsIgnoreCase("player")) {
            this.creatureType = CreatureType.PLAYER;
        } else if (creatureType.equalsIgnoreCase("monster")) {
            this.creatureType = CreatureType.MONSTER;
        } else {
            this.creatureType = CreatureType.NPC;
        }

        this.attributes = attributesFromString(block_lines[2], block_lines[3]);

        this.stats = statsFromString(block_lines[4]);
        this.proficiencies = proficienciesFromString(block_lines[5]);
        this.inventory = inventoryFromString(block_lines[6]);
        this.equipmentSlots = equipmentSlotsFromString(block_lines[7]);
    }

    // examples include a players username or goblin1 ect.
    public String name;
    // see the enums file
    public CreatureType creatureType;

    // public MonsterType monsterType;// I dont know if we'll need this

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
    public HashMap<EquipmentSlots, Item> equipmentSlots;

    // TODO: add a statblock class mapped by creature type or username containing:
    // creatureType,attributes, modifiers, stats, proficiencies, inventory, and
    // an array of Items called equipped
    // I think this would allow us to store a logged off user
    // and generic stats for creatures

    @Override
    public String toString() {
        return name + "\n" +
                creatureType.toString() + "\n" +
                attributes.toString() + "\n" +
                stats.toString() + "\n" +
                proficiencies.toString() + "\n" +
                inventory.toStoreString() + "\n" +
                equipmentSlotsToString() + "\n";

    }

    public String getName() {
        return name;
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

    private AttributeBlock attributesFromString(String scoreline, String modline) {
        scoreline = scoreline.substring(1, scoreline.length() - 1);
        scoreline = scoreline.replace("}", "");
        String[] scorepairs = scoreline.split(",");
        AttributeBlock attrBlock = new AttributeBlock();

        for (int i = 0; i < Attributes.values().length; i++) {
            String[] key_val = scorepairs[i].split("=");
            attrBlock.setScore(Attributes.valueOf(key_val[0].trim()), Integer.valueOf(key_val[1].trim()));
        }

        modline = modline.substring(1, modline.length() - 1);
        modline = modline.replace("}", "");
        String[] modpairs = modline.split(",");

        for (int i = 0; i < Attributes.values().length; i++) {
            String[] key_val = modpairs[i].split("=");
            Attributes key = Attributes.valueOf(key_val[0].trim());
            Integer value = Integer.valueOf(key_val[1].trim());
            if (attrBlock.getMod(key) != value) {
                Integer diff = value - attrBlock.getMod(key);
                attrBlock.setModBonus(key, diff);
            }
        }
        return attrBlock;
    }

    private HashMap<Stats, Integer> statsFromString(String line) {
        HashMap<Stats, Integer> stats = new HashMap<>();
        line = line.substring(1, line.length() - 1);
        line = line.replace("}", "");
        System.out.println(line);

        String[] pairs = line.split(",");

        for (int i = 0; i < Stats.values().length; i++) {
            String[] key_val = pairs[i].split("=");
            stats.put(Stats.valueOf(key_val[0].replace(" ", "")), Integer.valueOf(key_val[1]));
        }

        return stats;
    }

    private HashSet<EquipmentTypes> proficienciesFromString(String line) {
        HashSet<EquipmentTypes> proficiencies = new HashSet<>();
        line = line.substring(1, line.length() - 1);
        String[] proficiencies_strings = line.split(",");
        for (String proficiencies_string : proficiencies_strings) {
            String proficiency = proficiencies_string.replace(" ", "");
            if (proficiency.equalsIgnoreCase("")) {
                continue;
            }
            proficiencies.add(EquipmentTypes.valueOf(proficiency));
        }

        return proficiencies;
    }

    private Inventory inventoryFromString(String line) {
        Inventory inventory = new Inventory();
        line = line.strip();
        String[] items = line.split(",");

        for (String s : items) {
            String item = s.replace(" ", "");
            Item instance = itemFromString(item);
            if (!(instance == null)) {
                inventory.addItem((Takeable) instance);
            }
        }

        return inventory;
    }

    private Item itemFromString(String itemName) {
        String path_to_items = "com.lhf.game.item.concrete.";
        Object item_instance = null;
        try {
            Class<?> clazz = Class.forName(path_to_items + itemName);
            Constructor<?> constructor = clazz.getConstructor(boolean.class);
            item_instance = constructor.newInstance(Boolean.TRUE);

        } catch (java.lang.NoClassDefFoundError | java.lang.ClassNotFoundException | java.lang.NoSuchMethodException
                | java.lang.IllegalAccessException | java.lang.InstantiationException
                | java.lang.reflect.InvocationTargetException e) {
            System.out.println(itemName + " not found in package " + path_to_items + "... skipping it.");
        }

        return (Item) item_instance;
    }

    private HashMap<EquipmentSlots, Item> equipmentSlotsFromString(String line) {

        HashMap<EquipmentSlots, Item> equipSlots = new HashMap<>();

        if (line.equals("{}")) {
            return equipSlots;
        }

        line = line.substring(1, line.length() - 1);
        System.out.println(line);
        String[] pairs = line.split(",");

        // pairs
        for (String pair : pairs) {
            String[] key_val = pair.split("=");
            if (!key_val[1].equals("empty")) {
                Item instance = itemFromString(key_val[1].strip().replace(" ", ""));
                equipSlots.put(EquipmentSlots.valueOf(key_val[0]), instance);
            }
        }

        return equipSlots;
    }

}