package com.lhf.game.creature.vocation;

import com.lhf.game.battle.MultiAttacker;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.resourcepools.IntegerResourcePool;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;

public class Fighter extends Vocation implements MultiAttacker {

    protected class Stamina extends IntegerResourcePool {

        protected Stamina() {
            super(22, level -> level / 2);
        }

        @Override
        public int getLevel() {
            return Fighter.this.level;
        }

    }

    public Fighter() {
        super(VocationName.FIGHTER);
    }

    @Override
    protected ResourcePool initPool() {
        return new Stamina();
    }

    @Override
    public Statblock createNewDefaultStatblock(String creatureRace) {
        Statblock built = new Statblock(creatureRace);

        built.getProficiencies().add(EquipmentTypes.LIGHTARMOR);
        built.getProficiencies().add(EquipmentTypes.MEDIUMARMOR);
        built.getProficiencies().add(EquipmentTypes.SHIELD);
        built.getProficiencies().add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        built.getProficiencies().add(EquipmentTypes.MARTIALWEAPONS);

        built.getInventory().addItem(new Longsword(true));
        built.getInventory().addItem(new LeatherArmor(false));
        built.getInventory().addItem(new HealPotion(true));
        built.getInventory().addItem(new Shield(true));

        // Set default stats
        built.getStats().put(Stats.MAXHP, 12);
        built.getStats().put(Stats.CURRENTHP, 12);
        built.getStats().put(Stats.AC, 11);
        built.getStats().put(Stats.XPWORTH, 500);

        built.getAttributes().setScore(Attributes.STR, 16);
        built.getAttributes().setScore(Attributes.DEX, 12);
        built.getAttributes().setScore(Attributes.CON, 14);
        built.getAttributes().setScore(Attributes.INT, 8);
        built.getAttributes().setScore(Attributes.WIS, 10);
        built.getAttributes().setScore(Attributes.CHA, 12);

        return built;
    }

    @Override
    public Attributes getAggrovationAttribute() {
        return Attributes.CHA;
    }

    @Override
    public int getAggrovationLevel() {
        return this.level;
    }

    @Override
    public String getMultiAttackerVocation() {
        return this.getName();
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
