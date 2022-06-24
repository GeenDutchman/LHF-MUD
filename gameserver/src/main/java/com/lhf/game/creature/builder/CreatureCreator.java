package com.lhf.game.creature.builder;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

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

        public void stepSucceeded(boolean succeeded);

        public String buildCreatureName();

        public String buildStatblockName();

        public CreatureFaction buildFaction();

        public AttributeBlock buildAttributeBlock();

        public HashMap<Stats, Integer> buildStats(AttributeBlock attrs);

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

    public static Statblock writeStatblock(Statblock towrite) {
        StatblockManager loader_unloader = new StatblockManager();
        loader_unloader.statblockToFile(towrite);
        try {
            return loader_unloader.statblockFromfile(towrite.getCreatureRace());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Statblock readStatblock(String statblockname) throws FileNotFoundException {
        StatblockManager loader_unloader = new StatblockManager();
        return loader_unloader.statblockFromfile(statblockname);
    }

    private static Statblock makeStatblock(CreatorAdaptor adapter) {
        Statblock built = new Statblock();

        // name
        built.setCreatureRace(adapter.buildStatblockName());

        // creature faction
        built.setFaction(adapter.buildFaction());

        // attributes
        built.setAttributes(adapter.buildAttributeBlock());

        // stats
        built.setStats(adapter.buildStats());

        // Adds proficiencies
        built.setProficiencies(adapter.buildProficiencies());

        // Adds items
        built.setInventory(adapter.buildInventory());

        built.setEquipmentSlots(adapter.equipFromInventory(built.getInventory()));

        Statblock test = new Statblock(built.toString());
        // System.out.println(test);

        test = CreatureCreator.writeStatblock(test);
        // System.err.println(test);
        adapter.close();
        // System.out.println("\nCreature Creation Complete!");
        return built;
    }

    public static Monster makeMonsterFromStatblock(CreatorAdaptor adapter) throws FileNotFoundException {

        String statblockname = adapter.buildStatblockName();

        Statblock monStatblock = CreatureCreator.readStatblock(statblockname);

        if (monStatblock == null) {
            return null;
        }

        String creaturename = adapter.buildCreatureName();

        return new Monster(creaturename, monStatblock);

    }

    public static Player makePlayer(PlayerCreatorAdaptor adapter) {

        Statblock playerStatblock = null;

        while (playerStatblock == null) {
            String statblockname = adapter.buildStatblockName();
            try {
                playerStatblock = CreatureCreator.readStatblock(statblockname);
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
        CreatureCreator.makeStatblock(cliAdaptor);
    }
}