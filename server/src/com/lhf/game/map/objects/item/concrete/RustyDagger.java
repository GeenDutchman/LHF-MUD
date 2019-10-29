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

public class RustyDagger extends Item implements Weapon {
    private static EquipType TYPE = EquipType.WEAPON;

    public RustyDagger(boolean isVisible) {
        super("Rusty Dagger", isVisible);
    }

    @Override
    public EquipType getType() {
        return TYPE;
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
    public WeaponSubtype getWeaponSubtype() {
        return WeaponSubtype.SIMPLE;
    }

    @Override
    public int rollToHit() {
        return Dice.roll(1, 20);
    }

    @Override
    public int rollDamage() {
        return Dice.roll(1, 4);
    }

    @Override
    public Attack rollAttack() {
        return new Attack(this.rollToHit()).addFlavorAndDamage("Piercing", this.rollDamage());
    }

    @Override
    public String performUsage() {
        return null; //TODO: get this to attack as well
    }
}
