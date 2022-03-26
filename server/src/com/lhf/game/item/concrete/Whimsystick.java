package com.lhf.game.item.concrete;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD6;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class Whimsystick extends Weapon {
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;
    private Map<String, Integer> equippingChanges;
    private int acBonus = 1;

    public Whimsystick(boolean isVisible) {
        super("Whimsystick", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.CLUB);
        damages = Arrays.asList(new DamageDice(1, DieType.SIX, this.getMainFlavor()));
        equippingChanges = new HashMap<>();
        equippingChanges.put(Stats.AC.toString(), this.acBonus);
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
    public Map<String, Integer> getEquippingChanges() {
        return this.equippingChanges;
    }

    @Override
    public String getDescription() {
        return "This isn't quite a quarterstaff, but also not a club...it is hard to tell. " +
                "But what you can tell is it seems to have a laughing aura around it, like it doesn't " +
                "care about what it does to other people...it's a whimsystick. \n" +
                this.printStats();
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
        if (chooser.rollDice().getTotal() <= 2) {
            attack = attack.addFlavorAndRoll(DamageFlavor.HEALING, chooser.rollDice());
        } else {
            for (DamageDice dd : this.getDamages()) {
                attack = attack.addFlavorAndRoll(dd.getFlavor(), dd.rollDice());
            }
        }
        return attack;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.MARTIAL;
    }
}
