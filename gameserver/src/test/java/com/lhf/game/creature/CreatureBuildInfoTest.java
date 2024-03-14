package com.lhf.game.creature;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.reflect.TypeToken;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.AItem;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.RustyDagger;
import com.lhf.game.serialization.GsonBuilderFactory;

public class CreatureBuildInfoTest {

    @Test
    void testSerialization() {
        CreatureBuildInfo s = new CreatureBuildInfo();
        s.setCreatureRace("goober");
        s.setAttributeBlock(new AttributeBlock());
        EnumMap<Stats, Integer> stats = new EnumMap<>(Stats.class);
        stats.put(Stats.MAXHP, 10);
        stats.put(Stats.AC, 10);
        s.setStats(stats);
        Inventory inv = new Inventory();
        Longsword longsword = new Longsword();
        inv.addItem(longsword);
        s.setInventory(inv);
        EnumMap<EquipmentSlots, Equipable> equipped = new EnumMap<>(EquipmentSlots.class);
        RustyDagger dagger = new RustyDagger();
        equipped.put(EquipmentSlots.WEAPON, dagger);
        s.setEquipmentSlots(equipped);

        Gson gson = GsonBuilderFactory.start().items().creatureInfoBuilders().prettyPrinting().build();
        String json = gson.toJson(s);
        System.out.println(json);
        Truth.assertThat(json).contains(s.getCreatureRace());
        for (String itemName : inv.getItemList()) {
            Truth.assertThat(json).contains(itemName);
        }
        for (AItem item : equipped.values()) {
            Truth.assertThat(json).contains(item.getName());
        }

        CreatureBuildInfo num2 = gson.fromJson(json, CreatureBuildInfo.class);
        Truth.assertThat(num2.getCreatureRace()).isEqualTo(s.getCreatureRace());
        Truth.assertThat(num2.getStats().get(Stats.MAXHP)).isEqualTo(s.getStats().get(Stats.MAXHP));
        for (String itemName : inv.getItemList()) {
            Truth.assertThat(num2.getInventory().hasItem(itemName)).isTrue();
        }
        System.out.println(
                num2.getEquipmentSlots().getOrDefault(EquipmentSlots.WEAPON, null).produceMessage().toString());
        Truth.assertThat(num2.getEquipmentSlots().getOrDefault(EquipmentSlots.WEAPON, null).produceMessage()
                .toString())
                .isEqualTo(s.getEquipmentSlots().getOrDefault(EquipmentSlots.WEAPON, null).produceMessage().toString());
    }

    @Test
    void testPlayerSerialization() {
        List<ICreatureBuildInfo> toBuild = new ArrayList<>();
        PlayerBuildInfo buildInfo = PlayerBuildInfo.getInstance(null);
        String name = NameGenerator.Generate(null);
        buildInfo.setName(name);
        buildInfo.setVocation(VocationName.HEALER);
        toBuild.add(buildInfo);
        Gson gson = GsonBuilderFactory.start().items().creatureInfoBuilders().prettyPrinting().build();
        String json = gson.toJson(toBuild);
        System.out.println(json);
        Truth.assertThat(json).contains(name);
    }

    @Test
    void testPlayerMinimumDeserialization() {
        JsonArray list = new JsonArray();
        JsonObject playerBuildInfo = new JsonObject();
        playerBuildInfo.addProperty("className", PlayerBuildInfo.class.getName());
        JsonObject creatureBuilder = new JsonObject();
        creatureBuilder.addProperty("className", CreatureBuildInfo.class.getName());
        creatureBuilder.addProperty("creatureRace", "Creature");
        final String name = NameGenerator.Generate(null);
        creatureBuilder.addProperty("name", name);
        creatureBuilder.addProperty("faction", CreatureFaction.PLAYER.toString());
        creatureBuilder.addProperty("vocation", VocationName.HEALER.toString());
        playerBuildInfo.add("creatureBuilder", creatureBuilder);
        list.add(playerBuildInfo);
        final String json = list.toString();
        System.out.println(json);
        Truth.assertThat(json).contains(name);
        Gson gson = GsonBuilderFactory.start().items().creatureInfoBuilders().prettyPrinting().build();
        final Type listType = new TypeToken<List<ICreatureBuildInfo>>() {
        }.getType();
        List<ICreatureBuildInfo> buildInfos = gson.fromJson(json, listType);
        System.out.println(buildInfos);

        final String buildInfoJson = gson.toJson(buildInfos);
        System.out.println(buildInfoJson);
        Truth.assertThat(buildInfoJson).contains(name);

    }
}
