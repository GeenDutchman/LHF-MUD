package com.lhf.game.creature;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.EnumSet;

import com.lhf.game.creature.Monster.MonsterBuilder;
import com.lhf.game.creature.builder.CLIAdaptor;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.server.client.user.User;

public class CreatureCreator {
    public interface CreatorAdaptor extends Closeable {

        public void stepSucceeded(boolean succeeded);

        public String buildCreatureName();

        public String buildStatblockName();

        public CreatureFaction buildFaction();

        public AttributeBlock buildAttributeBlock();

        public EnumMap<Stats, Integer> buildStats(AttributeBlock attrs);

        public EnumSet<EquipmentTypes> buildProficiencies();

        public Inventory buildInventory();

        public EnumMap<EquipmentSlots, Equipable> equipFromInventory(Inventory inventory);

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

        // attributes
        built.setAttributes(adapter.buildAttributeBlock());

        // stats
        built.setStats(adapter.buildStats(built.getAttributes()));

        // Adds proficiencies
        built.setProficiencies(adapter.buildProficiencies());

        // Adds items
        built.setInventory(adapter.buildInventory());

        built.setEquipmentSlots(adapter.equipFromInventory(built.getInventory()));

        System.out.println(built.toString());

        built = CreatureCreator.writeStatblock(built);
        // System.err.println(test);
        adapter.close();
        // System.out.println("\nCreature Creation Complete!");
        return built;
    }

    private static final AIRunner aiRunner = new GroupAIRunner(false);

    public static Monster makeMonsterFromStatblock(CreatorAdaptor adapter) throws FileNotFoundException {

        String statblockname = adapter.buildStatblockName();

        Statblock monStatblock = CreatureCreator.readStatblock(statblockname);

        if (monStatblock == null) {
            return null;
        }

        MonsterBuilder builder = MonsterBuilder.getInstance(CreatureCreator.aiRunner);

        builder.setName(adapter.buildCreatureName());

        builder.setStatblock(monStatblock);

        return builder.build();
    }

    public static NonPlayerCharacter makeNPC() {
        NonPlayerCharacter.NPCBuilder builder = NonPlayerCharacter.getNPCBuilder(aiRunner);

        return builder.build();
    }

    public static DungeonMaster makeDM(String name) {
        DungeonMaster.DungeonMasterBuilder builder = DungeonMaster.DungeonMasterBuilder
                .getInstance(CreatureCreator.aiRunner);

        builder.setName(name);

        return builder.build();
    }

    public static Player makePlayer(PlayerCreatorAdaptor adapter) {
        Player.PlayerBuilder builder = Player.PlayerBuilder.getInstance(adapter.buildUser());

        builder.setStatblock(CreatureCreator.makeStatblock(adapter));

        while (builder.getStatblock() == null) {
            String statblockname = adapter.buildStatblockName();
            try {
                builder.setStatblock(CreatureCreator.readStatblock(statblockname));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                adapter.stepSucceeded(false);
            }
        }

        builder.setVocation(adapter.buildVocation());

        Player p = builder.build();

        return p;

    }

    public static void main(String[] args) {
        CLIAdaptor cliAdaptor = new CLIAdaptor();
        CreatureCreator.makeStatblock(cliAdaptor);
    }
}