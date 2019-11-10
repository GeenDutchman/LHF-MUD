package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Attack;
import com.lhf.game.map.objects.item.interfaces.Weapon;
import com.lhf.game.shared.dice.Dice;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class RustyDagger extends Weapon {

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible);
    }

    @Override
    public List<EquipmentTypes> getType() {
        List<EquipmentTypes> result = new ArrayList<>();
        result.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        result.add(EquipmentTypes.DAGGER);
        return result;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        List<EquipmentSlots> result = new ArrayList<>();
        result.add(EquipmentSlots.WEAPON);
        return result;
    }

    @Override
    public List<Pair<String, Integer>> equip() {
        return new ArrayList<>(0); // changes nothing
    }

    @Override
    public List<Pair<String, Integer>> unequip() {
        return new ArrayList<>(0); // changes nothing
    }

    @Override
    public String getDescription() {
        return "Rusty Dagger to stab monsters with";
    }

    @Override
    public int rollToHit() {
        return Dice.getInstance().d20(1);
    }

    @Override
    public int rollDamage() {
        return Dice.getInstance().d4(1);
    }

    @Override
    public Attack rollAttack() {
        return new Attack(this.rollToHit()).addFlavorAndDamage("Piercing", this.rollDamage());
    }
}
