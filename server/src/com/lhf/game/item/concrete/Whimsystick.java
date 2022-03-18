package com.lhf.game.item.concrete;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

import java.util.*;

public class Whimsystick extends Weapon {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;
    private int acBonus = 1;

    public Whimsystick(boolean isVisible) {
        super("Whimsystick", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.CLUB);
        damages = Arrays.asList(new DamageDice(1, DieType.SIX, this.getMainFlavor()));
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
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.MAGICAL_BLUDGEONING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return damages;
    }

    @Override
    public Attack modifyAttack(Attack attack) {
        Dice chooser = new DiceD6(1);
        if (chooser.roll() <= 2) {
            attack = attack.addFlavorAndDamage(DamageFlavor.HEALING, -1 * chooser.roll());
        } else {
            for (DamageDice dd : this.getDamages()) {
                attack = attack.addFlavorAndDamage(dd.getFlavor(), dd.roll());
            }
        }
        return attack;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.MARTIAL;
    }
}
