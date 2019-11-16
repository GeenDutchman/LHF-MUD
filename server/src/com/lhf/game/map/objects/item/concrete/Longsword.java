package com.lhf.game.map.objects.item.concrete;

import com.lhf.game.Attack;
import com.lhf.game.map.objects.item.interfaces.Weapon;
import com.lhf.game.shared.dice.Dice;
import com.lhf.game.shared.enums.EquipmentSlots;
import com.lhf.game.shared.enums.EquipmentTypes;

import java.util.*;

public class Longsword extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public Longsword(boolean isVisible) {
        super("Sword", isVisible);

        slots = Arrays.asList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
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
        return new Attack(this.rollToHit(), "").addFlavorAndDamage("Slashing", this.rollDamage());
    }

    @Override
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public String printWhichTypes() {
        StringJoiner sj = new StringJoiner(",");
        sj.setEmptyValue("none needed!");
        for (EquipmentTypes type : types) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    @Override
    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(",");
        sj.setEmptyValue("no slot!");
        for (EquipmentSlots slot : slots) {
            sj.add(slot.toString());
        }
        return sj.toString();
    }

    @Override
    public Map<String, Integer> equip() {
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public Map<String, Integer> unequip() {
        return new HashMap<>(0); // changes nothing
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("This is a nice, long, shiny sword.  It's a bit simple though...");
        sb.append("This can be equipped to: ").append(printWhichSlots());
        //sb.append("And best used if you have these proficiencies: ").append(printWhichTypes());
        //TODO: should this describe that it does 1d6 damage?
        return sb.toString();
    }

}
