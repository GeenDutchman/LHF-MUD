package com.lhf.game.creature.builder;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.creature.ICreatureBuildInfoVisitor;
import com.lhf.game.creature.CreatureCreator.CreatorAdaptor;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Takeable;

public class CLIAdaptor implements CreatorAdaptor {

    private Scanner input;

    public CLIAdaptor() {
        this.input = new Scanner(System.in);
    }

    @Override
    public int menuChoice(List<String> choices) {
        String chosen = null;
        do {
            System.out.print("Choose one of:\n");
            StringJoiner sj = new StringJoiner(", ").setEmptyValue("No choices!!");
            for (String choice : choices) {
                sj.add(choice);
            }
            System.out.println(sj.toString());
            chosen = this.input.nextLine().toLowerCase().trim();
        } while (chosen == null || chosen.isBlank());
        for (int i = 0; i < choices.size(); i++) {
            if (chosen.equalsIgnoreCase(choices.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void stepSucceeded(boolean succeeded) {
        if (succeeded) {
            System.out.println("Ok, that worked.");
        }
        System.out.println("That didn't work, you may have to try again.");
    }

    @Override
    public String getName() {
        Boolean valid;
        String name;
        System.out.println("Does this creature have a name?");
        if (!this.yesOrNo()) {
            return null;
        }
        do {
            System.out.print("Please type the creature's name: ");
            name = this.input.nextLine();

            System.out.print("The name is: " + name + " is this correct?");

            valid = this.yesOrNo();

        } while (!valid);
        return name;
    }

    /**
     * @deprecated Prefer {@link #getName()}
     */
    @Deprecated(forRemoval = false)
    @Override
    public String getRawName() {
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
    public CreatureFaction getFaction() {
        Boolean valid;
        String factionName;
        CreatureFaction faction;
        do {
            System.out.print(
                    String.format("Please indicate the creature's faction %s: ", CreatureFaction.values().toString()));
            factionName = this.input.nextLine();
            faction = CreatureFaction.getFaction(factionName);

            if (faction != null) {
                System.out.print(String.format("The creature faction is: %s is this correct?", faction));
                valid = this.yesOrNo();
            } else {
                System.err.println(String.format("Invalid Creature faction, restarting from last prompt. From %s",
                        CreatureFaction.values().toString()));
                valid = Boolean.FALSE;
            }

        } while (!valid);

        return faction;
    }

    @Override
    public AttributeBlock getAttributeBlock() {
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
    public void buildStats(AttributeBlock attrs, EnumMap<Stats, Integer> stats) {
        Boolean valid;
        if (stats == null) {
            stats = new EnumMap<>(Stats.class);
        }
        do {
            int max_hp;
            int xp_worth;
            float cr;
            System.out.print(
                    "What is the HP (integer) xp worth(integer) and approximate CR (float) (each value should be separated by a space): ");
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

            stats.put(Stats.MAXHP, max_hp + attrs.getMod(Attributes.CON));
            stats.put(Stats.CURRENTHP, max_hp + attrs.getMod(Attributes.CON));

            stats.put(Stats.XPWORTH, xp_worth + Math.abs(attrs.getMod(Attributes.INT)));
            stats.put(Stats.XPEARNED, 0);
            stats.put(Stats.PROFICIENCYBONUS, 0);
            // NOTE: 1 CR is roughly 4 level one players
            stats.put(Stats.AC, 10 + attrs.getMod(Attributes.DEX));

            System.out.print("Given max HP of " + max_hp + " and cr of " + cr +
                    " max/current hp is: " + stats.get(Stats.CURRENTHP) + "" +
                    " xp worth is: " + stats.get(Stats.XPWORTH) + " Is this correct?(yes,no) ");
            valid = this.yesOrNo();

        } while (!valid);
        return;
    }

    @Override
    /**
     * Returns the default stats. Use {@link #buildStats(AttributeBlock, EnumMap)}
     * if you want to build them
     */
    public EnumMap<Stats, Integer> getStats() {
        EnumMap<Stats, Integer> stats = new EnumMap<>(Stats.class);
        ICreatureBuildInfo.setDefaultStats(stats);
        return stats;
    }

    @Override
    public EnumSet<EquipmentTypes> getProficiencies() {
        String proficiency_string;
        EquipmentTypes proficiency;
        EnumSet<EquipmentTypes> proficiencies = EnumSet.noneOf(EquipmentTypes.class);
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
    public Inventory getInventory() {
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
    public void equipFromInventory(Inventory inventory, EnumMap<EquipmentSlots, Equipable> equipmentSlots) {
        String item_slot_string;
        if (equipmentSlots == null) {
            equipmentSlots = new EnumMap<>(EquipmentSlots.class);
        }
        if (inventory == null) {
            inventory = new Inventory();
        }

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
                Optional<IItem> optItem = inventory.removeItem(pair[0].strip());
                if (optItem.isPresent() && optItem.get() instanceof Equipable) {
                    equipmentSlots.put(slot, (Equipable) optItem.get());
                } else {
                    System.out.println(pair[0] + " is not a valid choice. Match the name exactly, ignoring case.");
                }

            } catch (java.lang.IllegalArgumentException e) {
                System.err.println(e.getMessage());
                System.err.println("Slots are: " + Arrays.toString(EquipmentSlots.values()));
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.err.println("Expected a comma between Item and Slot.");
            }

        }
        return;
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

    @Override
    public String getClassName() {
        throw new UnsupportedOperationException("CLI adapter does not give a class name");
    }

    @Override
    public CreatureBuilderID getCreatureBuilderID() {
        throw new UnsupportedOperationException("CLI adapter does not give a builder id");
    }

    @Override
    public String getCreatureRace() {
        Boolean valid;
        String name;
        do {
            System.out.print("Please type the creature's race: ");
            name = this.input.nextLine();

            System.out.print("The race is: " + name + " is this correct?");

            valid = this.yesOrNo();

        } while (!valid);
        return name;
    }

    /**
     * @deprecated prefer {@link #equipFromInventory(Inventory, EnumMap)}
     */
    @Deprecated(forRemoval = false)
    @Override
    public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
        return new EnumMap<>(EquipmentSlots.class);
    }

    private EnumSet<DamageFlavor> buildFlavors(DamgeFlavorReaction dfr) {
        String name;
        EnumSet<DamageFlavor> flavors = EnumSet.noneOf(DamageFlavor.class);
        System.out.println(DamageFlavor.values());
        while (true) {
            name = this.input.nextLine().strip();
            if ("done".equalsIgnoreCase(name)) {
                return flavors;
            }
            DamageFlavor flavor = DamageFlavor.getDamageFlavor(name);
            if (flavor == null) {
                System.out.println(
                        String.format("'%s' is not a valid damage flavor, try again (or 'done' to be done)", name));
            } else {
                flavors.add(flavor);
                System.out.println(String.format("Current: %s", flavors));
            }
        }
    }

    /**
     * @deprecated prefer to use
     *             {@link #equipFromInventory(Inventory, EnumMap)} because it
     *             is
     *             difficult to specify an effect out of nowhere.
     */
    @Deprecated(forRemoval = false)
    @Override
    public Set<CreatureEffect> getCreatureEffects() {
        return new TreeSet<>();
    }

    @Override
    public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
        EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> reactions = new EnumMap<>(DamgeFlavorReaction.class);
        ICreatureBuildInfo.setDefaultFlavorReactions(reactions);
        System.out.println(String.format("Use default damage flavor reactions? %s", reactions));
        if (this.yesOrNo()) {
            return reactions;
        }
        reactions.clear();
        for (final DamgeFlavorReaction dfr : DamgeFlavorReaction.values()) {
            System.out.println("The creature is " + dfr.name()
                    + " to which of the following damage types (enter 'done' when done)?");
            reactions.put(dfr, this.buildFlavors(dfr));
        }
        return reactions;
    }

    @Override
    public VocationName getVocation() {
        System.out.println("Should the creature have a Vocation?");
        if (this.yesOrNo()) {
            VocationName vName = null;
            do {
                String name = input.nextLine().trim();
                vName = VocationName.getVocationName(name);
                if (vName == null) {
                    System.out.println(String.format("'%s' is not a valid vocation name, try one of %s", name,
                            VocationName.values()));
                }
            } while (vName == null);
            return vName;
        }
        return null;
    }

    @Override
    public Integer getVocationLevel() {
        System.out.println("Should the creature have a vocation higher than default?");
        if (this.yesOrNo()) {
            int level = -1;
            while (level <= 0) {
                System.out.println("Level should be greater than 0!");
                level = input.nextInt();
            }
            return level;
        }
        return null;
    }

    @Override
    public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
        throw new UnsupportedOperationException("Unimplemented method 'acceptBuildInfoVisitor'");
    }

}
