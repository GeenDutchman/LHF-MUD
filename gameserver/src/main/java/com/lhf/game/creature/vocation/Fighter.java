package com.lhf.game.creature.vocation;

import com.lhf.game.battle.MultiAttacker;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.Statblock.StatblockBuilder;
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
    public Vocation copy() {
        return new Fighter();
    }

    @Override
    protected ResourcePool initPool() {
        return new Stamina();
    }

    @Override
    public StatblockBuilder createNewDefaultStatblock(String creatureRace) {
        StatblockBuilder builder = Statblock.getBuilder().setCreatureRace(creatureRace);

        builder.addProficiency(EquipmentTypes.LIGHTARMOR);
        builder.addProficiency(EquipmentTypes.MEDIUMARMOR);
        builder.addProficiency(EquipmentTypes.SHIELD);
        builder.addProficiency(EquipmentTypes.SIMPLEMELEEWEAPONS);
        builder.addProficiency(EquipmentTypes.MARTIALWEAPONS);

        builder.addItemToInventory(new Longsword(true));
        builder.addItemToInventory(new LeatherArmor(false));
        builder.addItemToInventory(new HealPotion(true));
        builder.addItemToInventory(new Shield(true));

        // Set default stats
        builder.setStat(Stats.MAXHP, 12);
        builder.setStat(Stats.CURRENTHP, 12);
        builder.setStat(Stats.AC, 11);
        builder.setStat(Stats.XPWORTH, 500);

        builder.setAttributeBlock(16, 12, 14, 8, 10, 12);

        return builder;
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
