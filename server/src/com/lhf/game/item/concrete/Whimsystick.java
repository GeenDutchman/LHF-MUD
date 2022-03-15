package com.lhf.game.item.concrete;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.Dice;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;

import java.util.*;

public class Whimsystick extends Weapon {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private int acBonus = 1;

    public Whimsystick(boolean isVisible) {
        super("Whimsystick", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.CLUB);
    }

    @Override
    public int rollToHit() {
        return Dice.getInstance().d20(1);
    }

    @Override
    public int rollDamage() {
        int amount = Dice.getInstance().d6(1);
        int switchIt = Dice.getInstance().d6(1);
        if (switchIt <= 2) {
            amount *= -1;
        }
        return amount;
    }

    @Override
    public Attack rollAttack() {
        Attack toReturn = new Attack(this.rollToHit(), "");
        int damage = rollDamage();
        if (damage < 0) {
            toReturn.addFlavorAndDamage("Healing", damage);
        } else {
            toReturn.addFlavorAndDamage(this.getMainFlavor(), damage);
        }
        return toReturn;
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
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", this.acBonus);
        return result;
    }

    @Override
    public Map<String, Integer> unequip() {
        Map<String, Integer> result = new HashMap<>();
        result.put("AC", -1 * this.acBonus);
        return result;
    }

    @Override
    public String getDescription() {
        return "This isn't quite a quarterstaff, but also not a club...it is hard to tell. " +
                "But what you can tell is it seems to have a laughing aura around it, like it doesn't " +
                "care about what it does to other people...it's a whimsystick. " +
                "This can be equipped to: " + printWhichSlots();
    }

    @Override
    public String getMainFlavor() {
        return "Bludgeoning";
    }
}
