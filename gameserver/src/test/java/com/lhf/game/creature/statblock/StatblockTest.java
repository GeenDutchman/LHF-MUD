package com.lhf.game.creature.statblock;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.EquipableDeserializer;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.TakeableDeserializer;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.RustyDagger;

public class StatblockTest {

    @Test
    void testSerialization() {

        Statblock s = new Statblock("goober");
        s.setCreatureRace("goober");
        s.setFaction(CreatureFaction.MONSTER);
        s.setAttributes(new AttributeBlock());
        HashMap<Stats, Integer> stats = new HashMap<>();
        stats.put(Stats.MAXHP, 10);
        stats.put(Stats.AC, 10);
        s.setStats(stats);
        Inventory inv = new Inventory();
        Longsword longsword = new Longsword(true);
        inv.addItem(longsword);
        s.setInventory(inv);
        HashMap<EquipmentSlots, Equipable> equipped = new HashMap<>();
        RustyDagger dagger = new RustyDagger(true);
        equipped.put(EquipmentSlots.WEAPON, dagger);
        s.setEquipmentSlots(equipped);

        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        Gson gson = gb.create();
        String json = gson.toJson(s);
        System.out.println(json);
        Truth.assertThat(json).contains(s.getCreatureRace());
        for (String itemName : inv.getItemList()) {
            Truth.assertThat(json).contains(itemName);
        }
        for (Item item : equipped.values()) {
            Truth.assertThat(json).contains(item.getName());
        }

        gb.registerTypeAdapter(Equipable.class, new EquipableDeserializer<Equipable>());
        gb.registerTypeAdapter(Takeable.class, new TakeableDeserializer<Takeable>());
        gb.registerTypeAdapter(Item.class, new ItemDeserializer<Item>());
        gson = gb.create();
        Statblock num2 = gson.fromJson(json, Statblock.class);
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
}
