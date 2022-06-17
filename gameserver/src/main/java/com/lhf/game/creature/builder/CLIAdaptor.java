package com.lhf.game.creature.builder;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;

import com.lhf.game.creature.builder.CreatureCreator.CreatorAdaptor;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.interfaces.Takeable;

public class CLIAdaptor implements CreatorAdaptor {

    private CreatureCreator creator;
    private Scanner input;

    public CLIAdaptor() {
        this.input = new Scanner(System.in);
    }

    @Override
    public void setCreator(CreatureCreator creator) {
        this.creator = creator;
    }

    @Override
    public CreatureCreator getCreator() {
        return this.creator;
    }

    @Override
    public void stepSucceeded(boolean succeeded) {
        if (succeeded) {
            System.out.println("Ok, that worked.");
        }
        System.out.println("That didn't work, you may have to try again.");
    }

    @Override
    public String buildCreatureName() {
        Boolean valid;
        String name;
        do {
            System.out.print("Please type the creature's name: ");
            name = this.input.nextLine();

            System.out.print("The name is: " + name + " is this correct?");

            valid = this.yesOrNo();

        } while (!valid);
        return name;
    }

    @Override
    public String buildStatblockName() {
        Boolean valid;
        String name;
        do {
            System.out.print("Please type the statblock name: ");
            name = this.input.nextLine();

            System.out.print("The name is: " + name + " is this correct?");

            valid = this.yesOrNo();

        } while (!valid);
        return name;
    }

    @Override
    public CreatureFaction buildFaction() {
        Boolean valid;
        String factionName;
        CreatureFaction faction;
        do {
            System.out.print("Please indicate the creature's faction (NPC/MONSTER): ");
            factionName = this.input.nextLine();
            faction = CreatureFaction.getFaction(factionName);

            if (faction != null) {
                System.out.print("The creature faction is: " + faction.toString() + " is this correct?");
                valid = this.yesOrNo();
            } else {
                System.err.println("Invalid Creature faction, restarting from last prompt.");
                valid = Boolean.FALSE;
            }

        } while (!valid);

        return faction;
    }

    @Override
    public AttributeBlock buildAttributeBlock() {
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
            this.input.nextLine(); // clear buffer
            valid = this.yesOrNo();
        } while (!valid);

        return attributes;
    }

    @Override
    public HashMap<Stats, Integer> buildStats() {
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

            this.input.nextLine(); // clears buffer

            stats.put(Stats.MAXHP, max_hp);
            stats.put(Stats.CURRENTHP, max_hp);

            stats.put(Stats.XPWORTH, xp_worth);
            stats.put(Stats.XPEARNED, 0);
            stats.put(Stats.PROFICIENCYBONUS, 0);
            // NOTE: 1 CR is roughly 4 level one players
            stats.put(Stats.AC, 10 + this.getCreator().getAttributes().getMod(Attributes.DEX));

            System.out.print("Given max HP of " + max_hp + " and cr of " + cr +
                    " max/current hp is: " + stats.get(Stats.CURRENTHP) + "" +
                    " xp worth is: " + stats.get(Stats.XPWORTH) + " Is this correct?(yes,no) ");
            valid = this.yesOrNo();

        } while (!valid);
        return stats;
    }

    @Override
    public HashSet<EquipmentTypes> buildProficiencies() {
        String proficiency_string;
        EquipmentTypes proficiency;
        HashSet<EquipmentTypes> proficiencies = new HashSet<>();
        while (true) {
            System.out.print("Enter one of the proficiencies or 'done' if there are no more to add: ");
            proficiency_string = this.input.nextLine();
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

    @Override
    public Inventory buildInventory() {
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

    @Override
    public HashMap<EquipmentSlots, Item> equipFromInventory(Inventory inventory) {
        String item_slot_string;
        HashMap<EquipmentSlots, Item> equipmentSlots = new HashMap<>();
        while (true) {
            System.out.print("Given: " + inventory.toStoreString()
                    + " \nIs there anything you would like to equip?(Item Name,slot or done) ");
            item_slot_string = input.nextLine().strip();
            if (item_slot_string.equalsIgnoreCase("done")) {
                break;
            }
            String[] pair = item_slot_string.split(",");
            try {

                EquipmentSlots slot = EquipmentSlots.valueOf(pair[1].strip().toUpperCase());
                Optional<Item> optItem = inventory.removeItem(pair[0].strip());
                if (optItem.isPresent()) {
                    equipmentSlots.put(slot, optItem.get());
                } else {
                    System.out.println(pair[0] + " is not a valid choice.  Match the name exactly, ignoring case.");
                }

            } catch (java.lang.IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.err.println("Slots are: " + Arrays.toString(EquipmentSlots.values()));
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.err.println("Expected a comma between Item and Slot.");
            }

        }
        return equipmentSlots;
    }

    @Override
    public Boolean yesOrNo() {
        System.out.println("yes or no?");
        String validation_response = this.input.nextLine().toLowerCase();
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

    @Override
    public void close() {
        this.input.close();
    }

}
