package com.lhf.game.creature.builder;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;

public class NpcCreator {

    private NpcCreator() {
    }

    // private int XP_FROM_CR = 200;
    // private int BONUS_XP_MOD = 50;

    public String buildName(Scanner input) {
        Boolean valid;
        String name;
        do {
            System.out.print("Welcome to creature creator, please type the creature's name: ");
            name = input.nextLine();

            System.out.print("The name is: " + name + " is this correct?");

            valid = validate(input);

        } while (!valid);
        return name;
    }

    public CreatureFaction buildFaction(Scanner input) {
        Boolean valid;
        String factionName;
        CreatureFaction faction;
        do {
            System.out.print("Please indicate the creature's faction (NPC/MONSTER): ");
            factionName = input.nextLine();
            faction = CreatureFaction.getFaction(factionName);

            if (faction != null) {
                System.out.print("The creature faction is: " + faction.toString() + " is this correct?");
                valid = validate(input);
            } else {
                System.err.println("Invalid Creature faction, restarting from last prompt.");
                valid = Boolean.FALSE;
            }

        } while (!valid);

        return faction;
    }

    public AttributeBlock buildAttributeBlock(Scanner input) {
        // attributes
        AttributeBlock attributes = new AttributeBlock();
        Boolean valid;
        do {
            System.out.print(
                    "Enter the attributes with a space between each number (STR DEX CON INT WIS CHA): ");
            try {
                Integer response_int;
                response_int = input.nextInt();
                attributes.setScore(Attributes.STR, response_int);
                response_int = input.nextInt();
                attributes.setScore(Attributes.DEX, response_int);
                response_int = input.nextInt();
                attributes.setScore(Attributes.CON, response_int);
                response_int = input.nextInt();
                attributes.setScore(Attributes.INT, response_int);
                response_int = input.nextInt();
                attributes.setScore(Attributes.WIS, response_int);
                response_int = input.nextInt();
                attributes.setScore(Attributes.CHA, response_int);

            } catch (java.util.InputMismatchException e) {
                System.err.println("Invalid input, expected 6 integers separated by spaces.");
                input.nextLine(); // clear buffer
                valid = Boolean.FALSE;
                continue;
            }

            System.out.println(attributes.toString());
            System.out.print(" is this correct? ");
            input.nextLine(); // clear buffer
            valid = validate(input);
        } while (!valid);

        return attributes;
    }

    public HashMap<Stats, Integer> buildStats(Scanner input, AttributeBlock attributes) {
        Boolean valid;
        HashMap<Stats, Integer> stats = new HashMap<>();
        do {
            int max_hp;
            int xp_worth;
            float cr;
            System.out.print(
                    "What is the max HP (integer) xp worth(integer) and approximate CR (float) (each value should be separated by a space): ");
            try {
                max_hp = input.nextInt();
                xp_worth = input.nextInt();
                cr = input.nextFloat();

            } catch (java.util.InputMismatchException e) {
                System.err.println(
                        "Invalid input, expected two integers and a float (ex float: 0.25 2.0) separated by spaces.");
                input.nextLine(); // clear buffer
                valid = Boolean.FALSE;
                continue;
            }

            input.nextLine(); // clears buffer

            stats.put(Stats.MAXHP, max_hp);
            stats.put(Stats.CURRENTHP, max_hp);

            stats.put(Stats.XPWORTH, xp_worth);
            stats.put(Stats.XPEARNED, 0);
            stats.put(Stats.PROFICIENCYBONUS, 0);
            // NOTE: 1 CR is roughly 4 level one players
            stats.put(Stats.AC, 10 + attributes.getMod(Attributes.DEX));

            System.out.print("Given max HP of " + max_hp + " and cr of " + cr +
                    " max/current hp is: " + stats.get(Stats.CURRENTHP) + "" +
                    " xp worth is: " + stats.get(Stats.XPWORTH) + " Is this correct?(yes,no) ");
            valid = validate(input);

        } while (!valid);

    }

    public HashSet<EquipmentTypes> buildProficiencies(Scanner input) {
        String proficiency_string;
        EquipmentTypes proficiency;
        HashSet<EquipmentTypes> proficiencies = new HashSet<>();
        while (true) {
            System.out.print("Enter one of the proficiencies or 'done' if there are no more to add: ");
            proficiency_string = input.nextLine();
            if (proficiency_string.equalsIgnoreCase("done")) {
                break;
            }
            try {
                proficiency = EquipmentTypes.valueOf(proficiency_string.toUpperCase());
                proficiencies.add(proficiency);
            } catch (java.lang.IllegalArgumentException e) {
                System.err.println(proficiency_string
                        + " is not contained in EquipmentTypes file, try again or come back once you add it to the file.");
            }
        }

        return proficiencies;
    }

    public Inventory buildInventory(Scanner input) {
        Inventory inventory = new Inventory();
        String item;
        // May need to replace this to be more adaptive?
        String path_to_items = "com.lhf.game.item.concrete.equipment.";
        while (true) {
            System.out.print(
                    "Enter one of their inventory items (including weapons and armor) (FilenameOfItem or done): ");
            item = input.nextLine();
            item = item.strip();

            if (item.equalsIgnoreCase("done")) {
                break;
            }

            try {
                Class<?> clazz = Class.forName(path_to_items + item);
                Constructor<?> constructor = clazz.getConstructor(boolean.class);
                Object item_instance = constructor.newInstance(Boolean.TRUE);
                inventory.addItem((Takeable) item_instance);

            } catch (java.lang.NoClassDefFoundError | java.lang.ClassNotFoundException | java.lang.NoSuchMethodException
                    | java.lang.IllegalAccessException | java.lang.InstantiationException
                    | java.lang.reflect.InvocationTargetException e) {
                System.err.println(item + " not found in package " + path_to_items
                        + " \nPlease enter a valid item class's filename with camelCase and all that jazz.");
            }

            System.out.println(inventory.toStoreString());

        }

        return inventory;
    }

    public void setEquipment(Creature creature, Scanner input) {
        String item_slot_string;
        while (true) {
            System.out.print("Given: " + creature.getInventory().toStoreString()
                    + " \nIs there anything you would like to equip?(Item Name,slot or done) ");
            item_slot_string = input.nextLine().strip();
            if (item_slot_string.equalsIgnoreCase("done")) {
                break;
            }
            String[] pair = item_slot_string.split(",");
            try {

                EquipmentSlots slot = EquipmentSlots.valueOf(pair[1].strip().toUpperCase());
                npc.equipItem(pair[0], slot);

            } catch (java.lang.IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.err.println("Slots are: " + Arrays.toString(EquipmentSlots.values()));
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.err.println("Expected a comma between Item and Slot.");
            }

        }
    }

    private void makeCreature() {
        Scanner input = new Scanner(System.in);

        // name
        String name = this.buildName(input);

        // creature faction

        CreatureFaction faction = this.buildFaction(input);

        // attributes
        AttributeBlock attributes = this.buildAttributeBlock(input);

        // stats
        HashMap<Stats, Integer> stats = this.buildStats(input, attributes);

        // Adds proficiencies
        HashSet<EquipmentTypes> proficiencies = this.buildProficiencies(input);

        // Adds items
        Inventory inventory = this.buildInventory(input);

        HashMap<EquipmentSlots, Item> equipmentSlots = new HashMap<>();

        Statblock creation = new Statblock(name, faction, attributes, stats, proficiencies, inventory,
                equipmentSlots);

        Creature npc = new Creature(name, creation);

        this.setEquipment(npc, input);

        System.out.print(creation);

        Statblock test = new Statblock(creation.toString());
        System.out.println(test);

        StatblockManager loader_unloader = new StatblockManager();
        loader_unloader.statblockToFile(test);
        try {
            test = loader_unloader.statblockFromfile(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.err.println(test);
        input.close();
        System.out.println("\nCreature Creation Complete!");
    }

    private Boolean validate(Scanner input) {
        System.out.println("yes or no?");
        String validation_response = input.nextLine().toLowerCase();
        if (validation_response.equals("yes") || validation_response.equals("no")) {
            if (validation_response.equals("yes")) {
                return Boolean.TRUE;
            } else {
                System.out.println("Restarting from last prompt.");
                return Boolean.FALSE;
            }
        } else {
            System.err.println("Invalid response, restarting from last prompt.");
            return Boolean.FALSE;
        }

    }

    public static void main(String[] args) {
        NpcCreator creator = new NpcCreator();
        creator.makeCreature();
    }
}