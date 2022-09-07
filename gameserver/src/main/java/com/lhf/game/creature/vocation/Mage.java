package com.lhf.game.creature.vocation;

import java.util.EnumSet;

import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.magic.CubeHolder;

public class Mage extends Vocation implements CubeHolder {
    public Mage() {
        super(VocationName.MAGE);
    }

    @Override
    protected EnumSet<EquipmentTypes> generateProficiencies() {
        EnumSet<EquipmentTypes> prof = EnumSet.noneOf(EquipmentTypes.class);
        // TODO: add proficiencies
        return prof;
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
        return new MultiRollResult(new DiceD20(1).rollDice()); // TODO: actual attack
    }

}
