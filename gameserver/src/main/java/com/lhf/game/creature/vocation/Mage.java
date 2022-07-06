package com.lhf.game.creature.vocation;

import java.util.HashSet;

import com.lhf.game.dice.DiceD20;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.magic.CubeHolder;

public class Mage extends Vocation implements CubeHolder {
    public Mage() {
        super("Mage", Mage.generateProficiencies());
    }

    private static HashSet<EquipmentTypes> generateProficiencies() {
        HashSet<EquipmentTypes> prof = new HashSet<>();
        // TOOD: add proficiencies
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
    public RollResult spellAttack() {
        return new DiceD20(1).rollDice(); // TODO: actual attack
    }

}
