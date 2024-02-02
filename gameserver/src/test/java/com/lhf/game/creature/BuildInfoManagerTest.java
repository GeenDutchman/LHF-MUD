package com.lhf.game.creature;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.equipment.BossClub;
import com.lhf.game.item.concrete.equipment.ChainMail;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;
import com.lhf.game.item.concrete.equipment.Shortsword;
import com.lhf.game.serialization.GsonBuilderFactory;

public class BuildInfoManagerTest {
        @Test
        void testStatblockFromfile() throws JsonIOException, JsonSyntaxException, IOException {
                MonsterBuildInfo goblinHardCoded = new MonsterBuildInfo();
                goblinHardCoded.setCreatureRace("goblin");
                goblinHardCoded.setAttributeBlock(8, 14, 10, 10, 8, 8);
                goblinHardCoded.setStats(
                                Map.of(Stats.MAXHP, 7, Stats.AC, 12, Stats.PROFICIENCYBONUS, 0, Stats.CURRENTHP, 7,
                                                Stats.XPWORTH, 50, Stats.XPEARNED, 0));
                goblinHardCoded.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS).addProficiency(EquipmentTypes.SHIELD)
                                .addProficiency(EquipmentTypes.LIGHTARMOR);
                goblinHardCoded.setEquipmentSlots(Map.of(EquipmentSlots.WEAPON, new Shortsword(), EquipmentSlots.SHIELD,
                                new Shield(), EquipmentSlots.ARMOR, new LeatherArmor()));

                MonsterBuildInfo hobGoblinHardCoded = new MonsterBuildInfo();
                hobGoblinHardCoded.setCreatureRace("hobgoblin");
                hobGoblinHardCoded.setAttributeBlock(13, 12, 12, 10, 10, 9);
                hobGoblinHardCoded.setStats(
                                Map.of(Stats.MAXHP, 12, Stats.AC, 11, Stats.PROFICIENCYBONUS, 0, Stats.CURRENTHP,
                                                12, Stats.XPWORTH, 100, Stats.XPEARNED, 0));
                hobGoblinHardCoded.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS)
                                .addProficiency(EquipmentTypes.SHIELD)
                                .addProficiency(EquipmentTypes.HEAVYARMOR)
                                .addProficiency(EquipmentTypes.MARTIALWEAPONS);
                hobGoblinHardCoded
                                .setEquipmentSlots(Map.of(EquipmentSlots.WEAPON, new Longsword(), EquipmentSlots.SHIELD,
                                                new Shield(), EquipmentSlots.ARMOR, new ChainMail()));

                MonsterBuildInfo bugbearHardCoded = new MonsterBuildInfo();
                bugbearHardCoded.setCreatureRace("bugbear");
                bugbearHardCoded.setAttributeBlock(15, 14, 13, 8, 11, 9);
                bugbearHardCoded.setStats(
                                Map.of(Stats.MAXHP, 28, Stats.AC, 12, Stats.PROFICIENCYBONUS, 0, Stats.CURRENTHP, 28,
                                                Stats.XPWORTH, 201, Stats.XPEARNED, 0));
                bugbearHardCoded.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS).addProficiency(EquipmentTypes.SHIELD)
                                .addProficiency(EquipmentTypes.LIGHTARMOR);
                bugbearHardCoded.setEquipmentSlots(Map.of(EquipmentSlots.WEAPON, new BossClub(), EquipmentSlots.SHIELD,
                                new Shield(), EquipmentSlots.ARMOR, new LeatherArmor()));

                BuildInfoManager manager = new BuildInfoManager();

                GsonBuilderFactory gsonFactory = GsonBuilderFactory.start().creatureInfoBuilders();

                // // Uncomment these for updating the files
                // manager.statblockToFile(gsonFactory, goblinHardCoded);
                // manager.statblockToFile(gsonFactory, bugbearHardCoded);
                // manager.statblockToFile(gsonFactory, hobGoblinHardCoded);

                MonsterBuildInfo goblin = manager.monsterBuildInfoFromFile(gsonFactory,
                                "goblin");
                MonsterBuildInfo bugbear = manager.monsterBuildInfoFromFile(gsonFactory,
                                "bugbear");
                MonsterBuildInfo hobgoblin = manager.monsterBuildInfoFromFile(gsonFactory,
                                "hobgoblin");

                Truth.assertThat(goblin).isEqualTo(goblinHardCoded);
                Truth.assertThat(hobgoblin).isEqualTo(hobGoblinHardCoded);
                Truth.assertThat(bugbear).isEqualTo(bugbearHardCoded);

        }
}
