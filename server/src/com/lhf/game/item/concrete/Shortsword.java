package com.lhf.game.item.concrete;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.Dice;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;

import java.util.*;

public class Shortsword extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;

    public Shortsword(boolean isVisible) {
        super("Shortsword", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
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
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("none needed!");
        for (EquipmentTypes type : types) {
            sj.add(type.toString());
        }
        return sj.toString();
    }

    @Override
    public String printWhichSlots() {
        StringJoiner sj = new StringJoiner(", ");
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
        //sb.append("And best used if you have these proficiencies: ").append(printWhichTypes());
        //TODO: should this describe that it does 1d6 damage?
        return "This is a nice, short, shiny sword with a leather grip.  It's a bit simple though..." +
                "This can be equipped to: " + printWhichSlots();
    }

}