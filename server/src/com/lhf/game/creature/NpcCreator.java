package com.lhf.game.creature;

import com.lhf.game.inventory.Inventory;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.concrete.LeatherArmor;
import com.lhf.game.map.objects.item.interfaces.Takeable;
import com.lhf.game.map.objects.item.interfaces.Usable;
import com.lhf.game.shared.enums.*;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class NpcCreator {

    private NpcCreator() {
    }

    private int XP_FROM_CR = 200;
    private int BONUS_XP_MOD = 50;

    private void makeCreature() {
        Scanner input = new Scanner(System.in);
        String validation_response ="";
        Boolean valid = Boolean.FALSE;
        String response_string;
        int response_int;

        String name = "default";
        CreatureType creatureType = null;
        HashMap<Attributes, Integer> attributes = new HashMap<>();
        HashMap<Attributes, Integer> modifiers;
        HashMap<Stats, Integer> stats = new HashMap<>();
        HashSet<EquipmentTypes> proficiencies = new HashSet<>();
        Inventory inventory = new Inventory();
        HashMap<EquipmentSlots, Item> equipmentSlots = new HashMap<>();


        // name
        do{
            System.out.print("Welcome to creature creator, please type the creature's name: ");
            name = input.nextLine();

            System.out.print("The name is: "+ name + " is this correct? (yes/no)" );

            validation_response = input.nextLine().toLowerCase();

            valid = validate(validation_response);

        }while (!valid);

        // creature type

        do{
            System.out.print("Please indicate the creature's type (NPC/MONSTER): ");
            response_string = input.nextLine();
            System.out.print("The creature type is: "+ response_string + " is this correct?(yes/no) ");
            validation_response = input.nextLine();

            valid = validate(validation_response);
            if(valid){
                if(response_string.equalsIgnoreCase(String.valueOf(CreatureType.MONSTER)) ||
                        response_string.equalsIgnoreCase(String.valueOf(CreatureType.NPC))){
                    if(response_string.equalsIgnoreCase(String.valueOf(CreatureType.MONSTER))){
                        creatureType = CreatureType.MONSTER;
                    }
                    else{
                        creatureType = CreatureType.NPC;
                    }
                }
                else{
                    valid = Boolean.FALSE;
                    System.out.println("Invalid Creature type, restarting from last prompt.");
                }
            }
        }while (!valid);

        // attributes
        do{
            System.out.print("Enter " + name+"'s attributes with a space between each number (STR DEX CON INT WIS CHA): ");
            try{

                for (int i =0; i <6; i++){
                        response_int = input.nextInt();
                        attributes.put(Attributes.values()[i],response_int);
                }

            }catch (java.util.InputMismatchException e){
                System.out.println("Invalid input, expected 6 integers separated by spaces.");
                input.nextLine(); // clear buffer
                valid = Boolean.FALSE;
                continue;
            }


            printAttributes(attributes);
            System.out.print(" is this correct?(yes/no): ");
            input.nextLine(); // clear buffer
            validation_response = input.nextLine();
            valid = validate(validation_response);
        }while(!valid);

        //modifiers
        modifiers = calculateModifiers(attributes);

        //stats
        do{
            int max_hp;
            int xp_worth;
            float cr ;
            System.out.print("What is the max HP (integer) xp worth(integer) and approximate CR (float) of "+
                    name+":(each value should be separated by a space) ");
            try{
                 max_hp = input.nextInt();
                 xp_worth = input.nextInt();
                 cr = input.nextFloat();

            }catch (java.util.InputMismatchException e){
                System.out.println("Invalid input, expected two integers and a float (ex float: 0.25 2.0) separated by spaces.");
                input.nextLine(); // clear buffer
                valid = Boolean.FALSE;
                continue;
            }

            input.nextLine(); //clears buffer

            stats.put(Stats.MAXHP,max_hp);
            stats.put(Stats.CURRENTHP,max_hp);

            stats.put(Stats.XPWORTH, xp_worth);
            //TODO: XpEarned, prof bonus
            stats.put(Stats.XPEARNED,0);
            stats.put(Stats.PROFICIENCYBONUS,0);
            // NOTE: 1 CR is roughly 4 level one players
            stats.put(Stats.AC, 10 + modifiers.get(Attributes.DEX));

            System.out.print("Given max HP of "+max_hp+" and cr of "+cr+
                    " max/current hp is: "+stats.get(Stats.CURRENTHP)+"" +
                    " xp worth is: "+stats.get(Stats.XPWORTH)+ " Is this correct?(yes,no) ");
            validation_response = input.nextLine();
            valid = validate(validation_response);

        }while(!valid);

        //Adds proficiencies
        String proficiency_string = "";
        EquipmentTypes proficiency = null;

        while(Boolean.TRUE) {
            System.out.print("Enter one of " + name + "'s proficiencies or done if there are no more to add: ");
            proficiency_string = input.nextLine();
            if (proficiency_string.equalsIgnoreCase("done")) {
                break;
            }
            try {
                proficiency = EquipmentTypes.valueOf(proficiency_string.toUpperCase());
                proficiencies.add(proficiency);
            } catch (java.lang.IllegalArgumentException e) {
                System.out.println(proficiency_string + " is not contained in EquipmentTypes file, try again or come back once you add it to the file.");
            }
        }

        //Adds items
        String item = "";
        //May need to replace this to be more adaptive?
        String path_to_items = "com.lhf.game.map.objects.item.concrete.";
        while (Boolean.TRUE){
            System.out.print("Enter one of " + name + "'s inventory items(including weapons and armor) or done if there are no more: ");
            item = input.nextLine();
            item = item.strip();

            if(item.equalsIgnoreCase("done")){
                break;
            }

            try {
                Class<?> clazz = Class.forName(path_to_items+item);
                Constructor<?>constructor = clazz.getConstructor();
                Object item_instance = constructor.newInstance();
                inventory.addItem((Takeable) item_instance);

            }catch (java.lang.NoClassDefFoundError |
                    java.lang.ClassNotFoundException | java.lang.NoSuchMethodException |
                    java.lang.IllegalAccessException | java.lang.InstantiationException
                    | java.lang.reflect.InvocationTargetException e){
                System.out.println(item+" not found in package "+path_to_items +" \nPlease enter a valid item class's filename with camelCase and all that jazz.");
            }

            System.out.println(inventory.toString());


        }


        //TODO: equipped stuff

        /*
        do{
            System.out.print("Given: " + inventory.toString() +" is there anything you would like to equip?(type item name or done) ");
        }while (!valid);
        */

        Statblock creation = new Statblock(name,creatureType,attributes,modifiers,stats,proficiencies,inventory,equipmentSlots);
        System.out.print(creation);

        Statblock test = new Statblock(creation.toString());
        System.out.println(test);


        System.out.println("\nCreature Creation Complete!");
    }

    private Boolean validate(String validation_response){
        if(validation_response.equals("yes") || validation_response.equals("no")){
            if( validation_response.equals("yes")){
                return Boolean.TRUE;
            }
            else{
                System.out.println("Restarting from last prompt.");
                return Boolean.FALSE;
            }
        }
        else{
            System.out.println("Invalid response, restarting from last prompt.");
            return Boolean.FALSE;
        }

    }

    private void printAttributes(HashMap<Attributes, Integer> attributes){
        for(int i =0; i<6; i ++){
            Attributes attribute_name = Attributes.values()[i];
            int attribute = attributes.get(attribute_name);

            if( i == 5){
                System.out.print(attribute_name +" = "+attribute);
            }
            else {
                System.out.print(attribute_name +" = "+attribute+", ");
            }
        }
    }

    private HashMap<Attributes, Integer> calculateModifiers(HashMap<Attributes, Integer> attributes){
        HashMap<Attributes, Integer> modifiers = new HashMap<>();
        for(int i = 0; i < 6; i++){
            Attributes attribute_name = Attributes.values()[i];
            int attribute = attributes.get(attribute_name);
            int modifier = (attribute - 10) / 2;
            modifiers.put(attribute_name,modifier);
        }

        return  modifiers;
    }

    public static void main(String[] args) {
        NpcCreator creator = new NpcCreator();
        creator.makeCreature();
    }
}