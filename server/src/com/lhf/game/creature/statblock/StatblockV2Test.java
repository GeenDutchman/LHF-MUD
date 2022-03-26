package com.lhf.game.creature.statblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.enums.CreatureType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemDeserializer;
import com.lhf.game.item.TakeableDeserializer;
import com.lhf.game.item.concrete.Longsword;
import com.lhf.game.item.concrete.RustyDagger;
import com.lhf.game.item.interfaces.Takeable;

import org.junit.jupiter.api.Test;

public class StatblockV2Test {

    @Test
    void testSerialization() {

        StatblockV2 sV2 = new StatblockV2();
        sV2.setCreatureRace("goober");
        sV2.setCreatureType(CreatureType.MONSTER);
        sV2.setAbilityScores(new AttributeBlock());
        sV2.setMaxHealth(10);
        sV2.setBaseArmorClass(10);
        Inventory inv = new Inventory();
        Longsword longsword = new Longsword(true);
        inv.addItem(longsword);
        sV2.setInventory(inv);
        HashMap<EquipmentSlots, Item> equipped = new HashMap<>();
        RustyDagger dagger = new RustyDagger(true);
        equipped.put(EquipmentSlots.WEAPON, dagger);
        sV2.setEquippedItems(equipped);

        GsonBuilder gb = new GsonBuilder().setPrettyPrinting();
        Gson gson = gb.create();
        String json = gson.toJson(sV2);
        System.out.println(json);
        assertTrue(json.contains(sV2.getCreatureRace()));
        for (String itemName : inv.getItemList()) {
            assertTrue(json.contains(itemName));
        }
        for (Item item : equipped.values()) {
            assertTrue(json.contains(item.getName()));
        }

        gb.registerTypeAdapter(Takeable.class, new TakeableDeserializer<Takeable>());
        gb.registerTypeAdapter(Item.class, new ItemDeserializer<Item>());
        gson = gb.create();
        StatblockV2 num2 = gson.fromJson(json, StatblockV2.class);
        assertEquals(sV2.getCreatureRace(), num2.getCreatureRace());
        assertEquals(sV2.getMaxHealth(), num2.getMaxHealth());
        for (String itemName : inv.getItemList()) {
            assertTrue(num2.getInventory().hasItem(itemName));
        }
        assertEquals(sV2.getEquippedItems().getOrDefault(EquipmentSlots.WEAPON, null).getDescription(),
                num2.getEquippedItems().getOrDefault(EquipmentSlots.WEAPON, null).getDescription());

    }
}
