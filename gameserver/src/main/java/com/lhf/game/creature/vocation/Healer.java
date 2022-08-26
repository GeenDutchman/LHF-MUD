package com.lhf.game.creature.vocation;

import java.util.HashSet;

import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.magic.CubeHolder;

public class Healer extends Vocation implements CubeHolder {
    public Healer() {
        super(VocationName.HEALER, Healer.generateProficiencies());
    }

    private static HashSet<EquipmentTypes> generateProficiencies() {
        HashSet<EquipmentTypes> prof = new HashSet<>();
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
