package com.lhf.game.creature.vocation;

import java.util.EnumSet;

import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.magic.CubeHolder;

public class DMV extends Vocation implements CubeHolder {

    public DMV() {
        super(VocationName.DUNGEON_MASTER);
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
    public Integer getCasterDifficulty() {
        return Integer.MAX_VALUE / 3;
    }

    @Override
    public MultiRollResult spellAttack() {
        return new MultiRollResult(new DiceD20(1).rollDice()).addBonus(Integer.MAX_VALUE / 15); // TODO: actual attack

    }

}