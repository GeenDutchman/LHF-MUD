package com.lhf.game.creature.vocation;

import java.util.EnumSet;

import com.lhf.game.EntityEffect;
import com.lhf.game.battle.MultiAttacker;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.magic.CubeHolder;

public class DMVocation extends Vocation implements CubeHolder, MultiAttacker {

    private class UnlimitedPool implements ResourcePool {

        @Override
        public void refresh() {
            // Unlimited power is always refreshed
        }

        @Override
        public int getLevel() {
            return DMVocation.this.level;
        }

        @Override
        public boolean reload(ResourceCost refill) {
            // Unlimited power is always full
            return true;
        }

        @Override
        public String print() {
            return "Power: UnlimitedPool";
        }

        @Override
        public boolean checkCost(ResourceCost costNeeded) {
            // Unlimited power can pay
            return true;
        }

        @Override
        public ResourceCost payCost(ResourceCost costNeeded) {
            // Unlimited power pays it
            return costNeeded;
        }

    }

    public DMVocation() {
        super(VocationName.DUNGEON_MASTER);
    }

    @Override
    public DMVocation copy() {
        DMVocation aCopy = new DMVocation();
        return aCopy;
    }

    @Override
    protected ResourcePool initPool() {
        return new UnlimitedPool();
    }

    @Override
    public Statblock createNewDefaultStatblock(String creatureRace) {
        Statblock built = new Statblock(creatureRace);

        built.setProficiencies(EnumSet.allOf(EquipmentTypes.class));

        built.getInventory().addItem(new HealPotion(true));

        // Set default stats
        built.getStats().put(Stats.MAXHP, Integer.MAX_VALUE / 3);
        built.getStats().put(Stats.CURRENTHP, Integer.MAX_VALUE / 3);
        built.getStats().put(Stats.AC, Integer.MAX_VALUE / 3);
        built.getStats().put(Stats.XPWORTH, Integer.MAX_VALUE / 3);
        built.getStats().put(Stats.PROFICIENCYBONUS, Integer.MAX_VALUE / 3);

        built.getAttributes().setScore(Attributes.STR, 100);
        built.getAttributes().setScore(Attributes.DEX, 100);
        built.getAttributes().setScore(Attributes.CON, 100);
        built.getAttributes().setScore(Attributes.INT, 100);
        built.getAttributes().setScore(Attributes.WIS, 100);
        built.getAttributes().setScore(Attributes.CHA, 100);

        return built;
    }

    @Override
    public String getCasterVocation() {
        return this.getName();
    }

    @Override
    public int getCastingBonus(final EntityEffect effect) {
        return Integer.MAX_VALUE / 15;
    }

    @Override
    public String printMagnitudes() {
        return "You are a DM, you can cast all the spells on your list.\n";
    }

    @Override
    public boolean useMagnitude(ResourceCost level) {
        return level != null;
    }

    @Override
    public EnumSet<ResourceCost> availableMagnitudes() {
        return EnumSet.allOf(ResourceCost.class);
    }

    @Override
    public Vocation onLevel() {
        // level as a DM??
        return this;
    }

    @Override
    public Vocation onRestTick() {
        return this;
    }

    @Override
    public String getMultiAttackerVocation() {
        return this.getName();
    }

    @Override
    public Attributes getAggrovationAttribute() {
        return Attributes.CHA;
    }

    @Override
    public int getAggrovationLevel() {
        return this.level;
    }

}
