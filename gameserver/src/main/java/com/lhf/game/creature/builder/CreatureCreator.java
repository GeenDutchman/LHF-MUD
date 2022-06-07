package com.lhf.game.creature.builder;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;

public class CreatureCreator {
    public interface CreatorAdaptor extends Closeable {
        public void setCreator(CreatureCreator creator);

        public CreatureCreator getCreator();

        public String buildName();

        public CreatureFaction buildFaction();

        public AttributeBlock buildAttributeBlock();

        public HashMap<Stats, Integer> buildStats();

        public HashSet<EquipmentTypes> buildProficiencies();

        public Inventory buildInventory();

        public void equipFromInventory(Creature creature);

        public void close();
    };

    private CreatorAdaptor adapter;

    private String name;
    private CreatureFaction faction;
    private AttributeBlock attributes;
    private HashMap<Stats, Integer> stats;
    private HashSet<EquipmentTypes> proficiencies;
    private Inventory inventory;
    private HashMap<EquipmentSlots, Item> equipSlots;

    private Statblock statblock;

    private CreatureCreator(CreatorAdaptor adapter) {
        this.adapter = adapter;
    }

    public void setProvider(CreatorAdaptor adapter) {
        this.adapter = adapter;
    }

    public String getName() {
        return name;
    }

    public CreatureFaction getFaction() {
        return faction;
    }

    public AttributeBlock getAttributes() {
        return attributes;
    }

    public HashMap<Stats, Integer> getStats() {
        return stats;
    }

    public HashSet<EquipmentTypes> getProficiencies() {
        return proficiencies;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public HashMap<EquipmentSlots, Item> getEquipSlots() {
        return equipSlots;
    }

    public Statblock getStatblock() {
        return statblock;
    }

    public Statblock writeStatblock(Statblock towrite) {
        StatblockManager loader_unloader = new StatblockManager();
        loader_unloader.statblockToFile(towrite);
        try {
            return loader_unloader.statblockFromfile(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Creature makeCreature() {
        this.adapter.setCreator(this);
        // name
        this.name = this.adapter.buildName();

        // creature faction
        this.faction = this.adapter.buildFaction();

        // attributes
        this.attributes = this.adapter.buildAttributeBlock();

        // stats
        this.stats = this.adapter.buildStats();

        // Adds proficiencies
        this.proficiencies = this.adapter.buildProficiencies();

        // Adds items
        this.inventory = this.adapter.buildInventory();

        HashMap<EquipmentSlots, Item> equipmentSlots = new HashMap<>();

        Statblock creation = new Statblock(name, faction, attributes, stats, proficiencies, inventory,
                equipmentSlots);

        Creature npc = new Creature(name, creation);

        this.adapter.equipFromInventory(npc);

        // System.out.print(creation);

        Statblock test = new Statblock(creation.toString());
        // System.out.println(test);

        test = this.writeStatblock(test);
        // System.err.println(test);
        this.adapter.close();
        // System.out.println("\nCreature Creation Complete!");
        return npc;
    }

    public static void main(String[] args) {
        CLIAdaptor cliAdaptor = new CLIAdaptor();
        CreatureCreator creator = new CreatureCreator(cliAdaptor);
        creator.makeCreature();
    }
}