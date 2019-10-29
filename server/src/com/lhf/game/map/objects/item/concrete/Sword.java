package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Attack;
import com.lhf.game.Dice;
import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.Weapon;
import com.lhf.game.map.objects.item.interfaces.WeaponSubtype;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Sword extends Item implements Weapon {
    public Sword(boolean isVisible) {
        super("Sword", isVisible);
    }

    @Override
    public WeaponSubtype getWeaponSubtype() {
        return WeaponSubtype.SIMPLE;
    }

    @Override
    public int rollToHit() {
        return Dice.roll(1, 20);
    }

    @Override
    public int rollDamage() {
        return Dice.roll(1, 6);
    }

    @Override
    public Attack rollAttack() {
        return new Attack(this.rollToHit()).addFlavorAndDamage("Slashing", this.rollDamage());
    }

    @Override
    public EquipType getType() {
        return EquipType.WEAPON;
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
        sb.append("This is a nice, shiny sword.  It's a bit simple though...");
        //TODO: should this describe that it does 1d6 damage?
        return sb.toString();
    }

    @Override
    public String performUsage() {
        return "You swung a sword..."; //TODO: generalize this
    }
}
