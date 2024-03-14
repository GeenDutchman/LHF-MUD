package com.lhf.game.creature;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.builder.CLIAdaptor;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class CreatureCreator {

    private static final BuildInfoManager loader_unloader = new BuildInfoManager();

    public interface CreatorAdaptor extends Closeable, ICreatureBuildInfo {
        public int menuChoice(List<String> choices);

        public void stepSucceeded(boolean succeeded);

        public void buildStats(AttributeBlock attrs, EnumMap<Stats, Integer> statsMap);

        public void equipFromInventory(Inventory inventory, EnumMap<EquipmentSlots, Equipable> equipmentSlots);

        public Boolean yesOrNo();

        public void close();
    };

    public static MonsterBuildInfo makeMonsterFromStatblock(CreatorAdaptor adapter) {

        MonsterBuildInfo builder = MonsterBuildInfo.getInstance();
        builder.setCreatureRace(adapter.getCreatureRace());
        builder.setName(adapter.getName());
        builder.setFaction(adapter.getFaction());
        builder.setAttributeBlock(adapter.getAttributeBlock());
        adapter.buildStats(builder.getAttributeBlock(), builder.getStats());
        builder.setProficiencies(adapter.getProficiencies());
        builder.setInventory(adapter.getInventory());
        adapter.equipFromInventory(builder.getInventory(), builder.getEquipmentSlots());
        for (final Entry<EquipmentSlots, Equipable> entry : builder.getEquipmentSlots().entrySet()) {
            builder.addEquipment(entry.getKey(), entry.getValue(), false);
        }
        builder.setDamageFlavorReactions(adapter.getDamageFlavorReactions());
        builder.setVocation(adapter.getVocation());
        builder.setVocationLevel(adapter.getVocationLevel());

        return builder;
    }

    public static INonPlayerCharacterBuildInfo makeNPC(CreatorAdaptor adapter) {
        INPCBuildInfo builder = NonPlayerCharacter.getNPCBuilder();
        builder.setCreatureRace(adapter.getCreatureRace());
        builder.setName(adapter.getName());
        builder.setFaction(adapter.getFaction());
        builder.setAttributeBlock(adapter.getAttributeBlock());
        adapter.buildStats(builder.getAttributeBlock(), builder.getStats());
        builder.setProficiencies(adapter.getProficiencies());
        builder.setInventory(adapter.getInventory());
        adapter.equipFromInventory(builder.getInventory(), builder.getEquipmentSlots());
        for (final Entry<EquipmentSlots, Equipable> entry : builder.getEquipmentSlots().entrySet()) {
            builder.addEquipment(entry.getKey(), entry.getValue(), false);
        }
        builder.setDamageFlavorReactions(adapter.getDamageFlavorReactions());
        builder.setVocation(adapter.getVocation());
        builder.setVocationLevel(adapter.getVocationLevel());
        return builder;
    }

    public static DungeonMasterBuildInfo makeDM(CreatorAdaptor adapter) {
        DungeonMaster.DungeonMasterBuildInfo builder = DungeonMaster.DungeonMasterBuildInfo
                .getInstance();
        builder.setCreatureRace(adapter.getCreatureRace());
        builder.setName(adapter.getName());
        builder.setFaction(adapter.getFaction());
        builder.setAttributeBlock(adapter.getAttributeBlock());
        adapter.buildStats(builder.getAttributeBlock(), builder.getStats());
        builder.setProficiencies(adapter.getProficiencies());
        builder.setInventory(adapter.getInventory());
        adapter.equipFromInventory(builder.getInventory(), builder.getEquipmentSlots());
        for (final Entry<EquipmentSlots, Equipable> entry : builder.getEquipmentSlots().entrySet()) {
            builder.addEquipment(entry.getKey(), entry.getValue(), false);
        }
        builder.setDamageFlavorReactions(adapter.getDamageFlavorReactions());
        builder.setVocation(adapter.getVocation());
        builder.setVocationLevel(adapter.getVocationLevel());
        return builder;
    }

    public static void main(String[] args) {
        CLIAdaptor cliAdaptor = new CLIAdaptor();
        int menuChoice = -1;
        ICreatureBuildInfo buildinfo = null;
        do {
            menuChoice = cliAdaptor.menuChoice(List.of("Quit", "Make Monster", "Make NPC", "Make DM"));
            switch (menuChoice) {
                case 0:
                    System.out.println("Goodbye!");
                    return;
                case 1:
                    buildinfo = CreatureCreator.makeMonsterFromStatblock(cliAdaptor);
                    break;
                case 2:
                    buildinfo = CreatureCreator.makeNPC(cliAdaptor);
                    break;
                case 3:
                    buildinfo = CreatureCreator.makeDM(cliAdaptor);
                    break;
                default:
                    System.out.println("That choice is not recognized, try again!");
                    continue;
            }
            if (menuChoice > 0 && menuChoice <= 3) {
                Boolean written = loader_unloader.statblockToFile(null, buildinfo);
                if (written) {
                    System.out.println("Successfully wrote file");
                } else {
                    System.err.println("Error writing file");
                }
            }
            System.out.println("Continuing to menu....");
        } while (menuChoice < 0);
    }
}