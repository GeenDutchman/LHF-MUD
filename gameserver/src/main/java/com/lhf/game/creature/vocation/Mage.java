package com.lhf.game.creature.vocation;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.creature.vocation.resourcepools.SlottedResourcePool;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.magic.CubeHolder;

public class Mage extends Vocation implements CubeHolder {
    private class SpellSlots extends SlottedResourcePool {

        private static EnumMap<ResourceCost, Integer> getSpellSlotsMax() {
            EnumMap<ResourceCost, Integer> maxes = new EnumMap<>(ResourceCost.class);
            maxes.put(ResourceCost.NO_COST, Integer.MAX_VALUE);
            maxes.put(ResourceCost.FIRST_MAGNITUDE, 4);
            maxes.put(ResourceCost.SECOND_MAGNITUDE, 3);
            maxes.put(ResourceCost.THIRD_MAGNITUDE, 3);
            maxes.put(ResourceCost.FOURTH_MAGNITUDE, 3);
            maxes.put(ResourceCost.FIVTH_MAGNITUDE, 3);
            maxes.put(ResourceCost.SIXTH_MAGNITUDE, 2);
            maxes.put(ResourceCost.SEVENTH_MAGNITUDE, 2);
            maxes.put(ResourceCost.EIGHTH_MAGNITUDE, 1);
            maxes.put(ResourceCost.NINTH_MAGNITUDE, 1);
            maxes.put(ResourceCost.TENTH_MAGNITUDE, 0);
            return maxes;
        }

        private static EnumMap<ResourceCost, IntUnaryOperator> getSpellSlotsRefreshers() {
            EnumMap<ResourceCost, IntUnaryOperator> maxes = new EnumMap<>(ResourceCost.class);
            maxes.put(ResourceCost.NO_COST, level -> Integer.MAX_VALUE);
            maxes.put(ResourceCost.FIRST_MAGNITUDE, level -> level >= 3 ? 4 : (level == 2 ? 3 : 2));
            maxes.put(ResourceCost.SECOND_MAGNITUDE, level -> level >= 4 ? 3 : (level == 3 ? 2 : 0));
            maxes.put(ResourceCost.THIRD_MAGNITUDE, level -> level >= 6 ? 3 : (level == 5 ? 3 : 0));
            maxes.put(ResourceCost.FOURTH_MAGNITUDE, level -> level >= 9 ? 3 : (level == 8 ? 2 : (level == 7 ? 1 : 0)));
            maxes.put(ResourceCost.FIVTH_MAGNITUDE,
                    level -> level >= 18 ? 3 : (level == 10 ? 2 : (level == 9 ? 1 : 0)));
            maxes.put(ResourceCost.SIXTH_MAGNITUDE, level -> level >= 19 ? 2 : (level >= 11 ? 1 : 0));
            maxes.put(ResourceCost.SEVENTH_MAGNITUDE, level -> level >= 20 ? 2 : (level >= 12 ? 1 : 0));
            maxes.put(ResourceCost.EIGHTH_MAGNITUDE, level -> level >= 15 ? 1 : 0);
            maxes.put(ResourceCost.NINTH_MAGNITUDE, level -> level >= 17 ? 1 : 0);
            maxes.put(ResourceCost.TENTH_MAGNITUDE, level -> 0);
            return maxes;
        }

        public SpellSlots() {
            super(SpellSlots.getSpellSlotsMax(), SpellSlots.getSpellSlotsRefreshers());
        }

        @Override
        public int getLevel() {
            return Mage.this.level;
        }

    }

    public Mage() {
        super(VocationName.MAGE);
    }

    @Override
    protected ResourcePool initPool() {
        return new SpellSlots();
    }

    @Override
    public String getCasterVocation() {
        return this.getName();
    }

    @Override
    public Integer getCasterDifficulty() {
        return 13; // TODO: actual difficulty
    }

    @Override
    public MultiRollResult spellAttack() {
        return new MultiRollResult.Builder().addRollResults(new DiceD20(1).rollDice()).Build(); // TODO: actual attack
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
        built.getAttributes().setScore(Attributes.DEX, 12);
        built.getAttributes().setScore(Attributes.CON, 10);
        built.getAttributes().setScore(Attributes.INT, 16);
        built.getAttributes().setScore(Attributes.WIS, 14);
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
