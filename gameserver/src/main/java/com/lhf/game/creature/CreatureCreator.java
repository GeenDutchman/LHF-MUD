package com.lhf.game.creature;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.EnumSet;

import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.builder.CLIAdaptor;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
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

    public static ICreatureBuildInfo writeStatblock(ICreatureBuildInfo towrite) {
        loader_unloader.statblockToFile(null, towrite);
        try {
            return loader_unloader.statblockFromfile(null, towrite.getCreatureRace());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ICreatureBuildInfo readStatblock(String statblockname) throws FileNotFoundException {
        return loader_unloader.statblockFromfile(null, statblockname);
    }

    private static ICreatureBuildInfo makeStatblock(CreatorAdaptor adapter) {
        CreatureBuildInfo built = new CreatureBuildInfo();

        // name
        built.setCreatureRace(adapter.buildStatblockName());

        // attributes
        built.setAttributeBlock(adapter.buildAttributeBlock());

        // stats
        built.setStats(adapter.buildStats(built.getAttributeBlock()));

        // Adds proficiencies
        built.setProficiencies(adapter.buildProficiencies());

        // Adds items
        built.setInventory(adapter.buildInventory());

        built.setEquipmentSlots(adapter.equipFromInventory(built.getInventory()));

        System.out.println(built.toString());

        built = (CreatureBuildInfo) CreatureCreator.writeStatblock(built);
        // System.err.println(test);
        adapter.close();
        // System.out.println("\nCreature Creation Complete!");
        return built;
    }

    private static final AIRunner aiRunner = new GroupAIRunner(false);
    private static final BuildInfoManager loader_unloader = new BuildInfoManager();

    public static IMonster makeMonsterFromStatblock(CreatorAdaptor adapter) throws FileNotFoundException {

        String statblockname = adapter.buildStatblockName();

        IMonsterBuildInfo monStatblock = (IMonsterBuildInfo) CreatureCreator.readStatblock(statblockname);

        if (monStatblock == null) {
            return null;
        }

        IMonsterBuildInfo builder = IMonsterBuildInfo.getInstance();

        builder.setName(adapter.buildCreatureName());

        CreatureFactory factory = CreatureFactory.fromAIRunner(null, null, aiRunner, false);
        factory.visit(builder);

        return factory.getBuiltCreatures().getIMonsters().first();
    }

    public static INonPlayerCharacter makeNPC() throws FileNotFoundException {
        INPCBuildInfo builder = NonPlayerCharacter.getNPCBuilder();
        CreatureFactory factory = CreatureFactory.fromAIRunner(null, null, aiRunner, false);
        factory.visit(builder);

        return factory.getBuiltCreatures().getINpcs().first();
    }

    public static DungeonMaster makeDM(String name) throws FileNotFoundException {
        DungeonMaster.DungeonMasterBuildInfo builder = DungeonMaster.DungeonMasterBuildInfo
                .getInstance();

        builder.setName(name);
        CreatureFactory factory = CreatureFactory.fromAIRunner(null, null, aiRunner, false);
        factory.visit(builder);

        return factory.getBuiltCreatures().getDungeonMasters().first();
    }

    public static void main(String[] args) {
        CLIAdaptor cliAdaptor = new CLIAdaptor();
        CreatureCreator.makeStatblock(cliAdaptor);
    }
}