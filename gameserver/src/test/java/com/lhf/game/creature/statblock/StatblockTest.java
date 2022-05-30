package com.lhf.game.creature.statblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.TakeableDeserializer;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.RustyDagger;
import com.lhf.game.item.interfaces.Takeable;

import org.junit.jupiter.api.Test;

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
        HashMap<EquipmentSlots, Item> equipped = new HashMap<>();
        RustyDagger dagger = new RustyDagger(true);
        equipped.put(EquipmentSlots.WEAPON, dagger);
        s.setEquipmentSlots(equipped);

        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        Gson gson = gb.create();
        String json = gson.toJson(s);
        System.out.println(json);
        assertTrue(json.contains(s.getCreatureRace()));
        for (String itemName : inv.getItemList()) {
            assertTrue(json.contains(itemName));
        }
        for (Item item : equipped.values()) {
            assertTrue(json.contains(item.getName()));
        }

        gb.registerTypeAdapter(Takeable.class, new TakeableDeserializer<Takeable>());
        gb.registerTypeAdapter(Item.class, new ItemDeserializer<Item>());
        gson = gb.create();
        Statblock num2 = gson.fromJson(json, Statblock.class);
        assertEquals(s.getCreatureRace(), num2.getCreatureRace());
        assertEquals(s.getStats().get(Stats.MAXHP), num2.getStats().get(Stats.MAXHP));
        for (String itemName : inv.getItemList()) {
            assertTrue(num2.getInventory().hasItem(itemName));
        }
        assertEquals(s.getEquipmentSlots().getOrDefault(EquipmentSlots.WEAPON, null).getDescription(),
                num2.getEquipmentSlots().getOrDefault(EquipmentSlots.WEAPON, null).getDescription());

    }
}
