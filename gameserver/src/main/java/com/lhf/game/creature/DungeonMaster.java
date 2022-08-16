package com.lhf.game.creature;

import java.util.HashMap;
import java.util.HashSet;

import com.lhf.game.creature.builder.CreatureCreator;
import com.lhf.game.creature.builder.CreatureCreator.PlayerCreatorAdaptor;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.server.client.user.User;

public class DungeonMaster extends NonPlayerCharacter implements PlayerCreatorAdaptor {
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

    CreatureCreator creatureCreator;

    public DungeonMaster(String name) {
        super(name, DungeonMaster.makeStatblock());
    }

    @Override
    public void stepSucceeded(boolean succeeded) {
        // TODO Auto-generated method stub

    }

    @Override
    public String buildCreatureName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String buildStatblockName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CreatureFaction buildFaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AttributeBlock buildAttributeBlock() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Stats, Integer> buildStats(AttributeBlock attrs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashSet<EquipmentTypes> buildProficiencies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory buildInventory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<EquipmentSlots, Equipable> equipFromInventory(Inventory inventory) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean yesOrNo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public User buildUser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vocation buildVocation() {
        // TODO Auto-generated method stub
        return null;
    }
}
