package com.lhf.game.creature;

import java.util.EnumMap;
import java.util.EnumSet;

import com.lhf.game.creature.CreatureCreator.PlayerCreatorAdaptor;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.DMV;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.server.client.user.User;

public class DungeonMaster extends NonPlayerCharacter implements PlayerCreatorAdaptor {

    CreatureCreator creatureCreator;

    public DungeonMaster(String name) {
        super(name, new DMV());
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
    public EnumMap<Stats, Integer> buildStats(AttributeBlock attrs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EnumSet<EquipmentTypes> buildProficiencies() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Inventory buildInventory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EnumMap<EquipmentSlots, Equipable> equipFromInventory(Inventory inventory) {
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
