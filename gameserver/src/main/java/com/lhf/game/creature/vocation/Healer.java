package com.lhf.game.creature.vocation;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.resourcepools.IntegerResourcePool;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.Attributes;
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
    protected ResourcePool initPool() {
        return new SpellPoints();
    }

    @Override
    public String getCasterVocation() {
        return this.getName();
    }

    @Override
    public Statblock createNewDefaultStatblock(String creatureRace) {
        Statblock built = new Statblock(creatureRace);
        built.getProficiencies().add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        built.getProficiencies().add(EquipmentTypes.LIGHTARMOR);

        built.getInventory().addItem(new LeatherArmor(false));
        built.getInventory().addItem(new HealPotion(true));

        // Set default stats
        built.getStats().put(Stats.MAXHP, 9);
        built.getStats().put(Stats.CURRENTHP, 9);
        built.getStats().put(Stats.AC, 11);
        built.getStats().put(Stats.XPWORTH, 500);

        built.getAttributes().setScore(Attributes.STR, 8);
        built.getAttributes().setScore(Attributes.DEX, 10);
        built.getAttributes().setScore(Attributes.CON, 12);
        built.getAttributes().setScore(Attributes.INT, 14);
        built.getAttributes().setScore(Attributes.WIS, 16);
        built.getAttributes().setScore(Attributes.CHA, 12);

        return built;
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
