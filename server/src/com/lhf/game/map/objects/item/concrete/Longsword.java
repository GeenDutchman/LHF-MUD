package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Attack;
import com.lhf.game.map.objects.item.interfaces.Weapon;
import com.lhf.game.shared.dice.Dice;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Longsword extends Weapon {
    public Longsword(boolean isVisible) {
        super("Sword", isVisible);
    }

    @Override
    public int rollToHit() {
        return Dice.getInstance().d20(1);
    }

    @Override
    public int rollDamage() {
        return Dice.getInstance().d6(1);
    }

    @Override
    public Attack rollAttack() {
        return new Attack(this.rollToHit()).addFlavorAndDamage("Slashing", this.rollDamage());
    }

    @Override
    public List<EquipmentTypes> getType() {
        List result = new ArrayList<EquipmentTypes>();
        result.add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        result.add(EquipmentTypes.LONGSWORD);
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
        StringBuilder sb = new StringBuilder();
        sb.append("This is a nice, long, shiny sword.  It's a bit simple though...");
        //TODO: should this describe that it does 1d6 damage?
        return sb.toString();
    }

}
