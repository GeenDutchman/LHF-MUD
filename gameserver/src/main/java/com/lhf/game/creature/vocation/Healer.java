package com.lhf.game.creature.vocation;

import java.util.EnumSet;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Healer.SpellPoints;
import com.lhf.game.creature.vocation.resourcepools.IntegerResourcePool;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
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
        if (level == null) {
            return false;
        } else if (level.toInt() > this.spellPoints.amount) {
            return false;
        }
        this.spellPoints.use(level.toInt());
        return true;
    }

    @Override
    public EnumSet<ResourceCost> availableMagnitudes() {
        int count = (this.level / 2) + (this.level % 2 != 0 ? 1 : 0);
        EnumSet<ResourceCost> available = EnumSet.of(ResourceCost.NO_COST);
        for (ResourceCost sl : ResourceCost.values()) {
            if (sl.toInt() <= count && this.spellPoints.amount >= sl.toInt()) {
                available.add(sl);
            }
        }
        return available;
    }

    @Override
    public Vocation onLevel() {
        this.level += 1;
        this.spellPoints = this.initSpellPoints();
        return this;
    }

    @Override
    public Vocation onRestTick() {
        this.spellPoints.amount += 1;
        if (this.spellPoints.amount > this.spellPoints.maxAmount) {
            this.spellPoints.amount = this.spellPoints.maxAmount;
        }
        return this;
    }

}
