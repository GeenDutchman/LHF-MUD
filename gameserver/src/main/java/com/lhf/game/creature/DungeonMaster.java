package com.lhf.game.creature;

import java.util.HashMap;
import java.util.HashSet;

import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.Stats;

public class DungeonMaster extends NonPlayerCharacter {
    private static Statblock makeStatblock() {
        AttributeBlock attributes = new AttributeBlock(100, 100, 100, 100, 100, 100);
        HashMap<Stats, Integer> stats = new HashMap<>();
        stats.put(Stats.MAXHP, Integer.MAX_VALUE);
        stats.put(Stats.CURRENTHP, Integer.MAX_VALUE);
        stats.put(Stats.AC, Integer.MAX_VALUE);
        stats.put(Stats.PROFICIENCYBONUS, Integer.MAX_VALUE);
        stats.put(Stats.XPEARNED, Integer.MAX_VALUE);
        stats.put(Stats.XPWORTH, Integer.MAX_VALUE);
        Statblock toMake = new Statblock("DungeonMaster", CreatureFaction.NPC, attributes, stats, new HashSet<>(),
                new Inventory(), new HashMap<>());
        return toMake;
    }

    public DungeonMaster(String name) {
        super(name, DungeonMaster.makeStatblock());
    }
}
