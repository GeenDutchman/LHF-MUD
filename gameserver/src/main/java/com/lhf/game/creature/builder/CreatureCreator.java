package com.lhf.game.creature.builder;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.server.client.user.User;

public class CreatureCreator {
    public interface CreatorAdaptor extends Closeable {
        public void setCreator(CreatureCreator creator);

        public CreatureCreator getCreator();

        public void stepSucceeded(boolean succeeded);

        public String buildCreatureName();

        public String buildStatblockName();

        public CreatureFaction buildFaction();

        public AttributeBlock buildAttributeBlock();

        public HashMap<Stats, Integer> buildStats();

        public HashSet<EquipmentTypes> buildProficiencies();

        public Inventory buildInventory();

        public HashMap<EquipmentSlots, Item> equipFromInventory(Inventory inventory);

        public Boolean yesOrNo();

        public void close();
    };

    public interface PlayerCreatorAdaptor extends CreatorAdaptor {
        public User buildUser();

        public Vocation buildVocation();
    }

    private String statblockname;
    private String creaturename;
    private CreatureFaction faction;
    private AttributeBlock attributes;
    private HashMap<Stats, Integer> stats;
    private HashSet<EquipmentTypes> proficiencies;
    private Inventory inventory;
    private HashMap<EquipmentSlots, Item> equipSlots;

    private Statblock statblock;

    private CreatureCreator() {
    }

    public String getStatblockName() {
        return statblockname;
    }

    public String getCreatureName() {
        return creaturename;
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
            return loader_unloader.statblockFromfile(statblockname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Statblock readStatblock(String statblockname) throws FileNotFoundException {
        StatblockManager loader_unloader = new StatblockManager();
        return loader_unloader.statblockFromfile(statblockname);
    }

    private Statblock makeStatblock(CreatorAdaptor adapter) {
        adapter.setCreator(this);
        // name
        this.statblockname = adapter.buildStatblockName();

        // creature faction
        this.faction = adapter.buildFaction();

        // attributes
        this.attributes = adapter.buildAttributeBlock();

        // stats
        this.stats = adapter.buildStats();

        // Adds proficiencies
        this.proficiencies = adapter.buildProficiencies();

        // Adds items
        this.inventory = adapter.buildInventory();

        HashMap<EquipmentSlots, Item> equipmentSlots = adapter.equipFromInventory(inventory);

        Statblock creation = new Statblock(statblockname, faction, attributes, stats, proficiencies, inventory,
                equipmentSlots);

        // System.out.print(creation);

        Statblock test = new Statblock(creation.toString());
        // System.out.println(test);

        test = this.writeStatblock(test);
        // System.err.println(test);
        adapter.close();
        // System.out.println("\nCreature Creation Complete!");
        return creation;
    }

    public Monster makeMonsterFromStatblock(CreatorAdaptor adapter) throws FileNotFoundException {
        adapter.setCreator(this);

        this.statblockname = adapter.buildStatblockName();

        Statblock monStatblock = this.readStatblock(this.statblockname);

        if (monStatblock == null) {
            return null;
        }

        this.creaturename = adapter.buildCreatureName();

        return new Monster(this.creaturename, monStatblock);

    }

    public Player makePlayer(PlayerCreatorAdaptor adapter) {
        adapter.setCreator(this);

        this.creaturename = adapter.buildCreatureName();

        Statblock playerStatblock = null;

        while (playerStatblock == null) {
            this.statblockname = adapter.buildStatblockName();
            try {
                playerStatblock = this.readStatblock(this.statblockname);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                adapter.stepSucceeded(false);
            }
        }

        Vocation vocation = adapter.buildVocation();

        Player p = new Player(adapter.buildUser(), playerStatblock, vocation);

        return p;

    }

    public static void main(String[] args) {
        CLIAdaptor cliAdaptor = new CLIAdaptor();
        CreatureCreator creator = new CreatureCreator();
        creator.makeStatblock(cliAdaptor);
    }
}