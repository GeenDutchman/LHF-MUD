package com.lhf.game.creature.vocation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.StatblockBuilder;
import com.lhf.game.creature.vocation.resourcepools.IntegerResourcePool;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.magic.CubeHolder;

public class Healer extends Vocation implements CubeHolder {
    protected class SpellPoints extends IntegerResourcePool {

        protected SpellPoints() {
            super(22, level -> {
                int calculated = 0;
                for (int i = 1; i <= level; i++) {
                    calculated += 1;
                    if (i < 7 && i % 2 != 0) {
                        calculated += 1;
                    }
                }
                return calculated;
            });
        }

        @Override
        public int getLevel() {
            return Healer.this.level;
        }

    }

    public Healer() {
        super(VocationName.HEALER);
    }

    @Override
    public Vocation copy() {
        return new Healer();
    }

    @Override
    protected ResourcePool initPool() {
        return new SpellPoints();
    }

    @Override
    public String getCasterVocation() {
        return this.getName();
    }

    @Override
    public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
        StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);
        builder.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS);
        builder.addProficiency(EquipmentTypes.LIGHTARMOR);

        builder.addItemToInventory(new LeatherArmor(true));
        builder.addItemToInventory(new HealPotion(true));

        // Set default stats
        builder.setStat(Stats.MAXHP, 9);
        builder.setStat(Stats.CURRENTHP, 9);
        builder.setStat(Stats.AC, 11);
        builder.setStat(Stats.XPWORTH, 500);

        builder.setAttributeBlock(8, 10, 12, 14, 16, 12);

        return builder;
    }

    @Override
    public String printMagnitudes() {
        return String.format("You have %s.\n", this.getResourcePool().print());
    }

    @Override
    public boolean useMagnitude(ResourceCost level) {
        ResourcePool pool = this.getResourcePool();
        if (pool == null) {
            return false;
        }
        if (level == null) {
            return false;
        }
        if (!pool.checkCost(level)) {
            return false;
        }
        ResourceCost paid = pool.payCost(level);
        if (paid == null) {
            return false;
        }
        if (level.compareTo(paid) >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public EnumSet<ResourceCost> availableMagnitudes() {
        ResourcePool pool = this.getResourcePool();
        if (pool == null) {
            return EnumSet.noneOf(ResourceCost.class);
        }
        return Arrays.asList(ResourceCost.values()).stream().filter(cost -> pool.checkCost(cost))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ResourceCost.class)));
    }

    @Override
    public Vocation onRestTick() {
        ResourcePool pool = this.getResourcePool();
        if (pool != null) {
            for (ResourceCost cost : ResourceCost.values()) {
                pool.reload(cost);
            }
        }
        return this;
    }

}
