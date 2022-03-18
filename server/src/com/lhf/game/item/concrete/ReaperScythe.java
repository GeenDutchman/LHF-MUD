package com.lhf.game.item.concrete;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

import java.util.*;

public class ReaperScythe extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;

    public ReaperScythe(boolean isVisible) {
        super("Reaper Scythe", isVisible);

        slots = Arrays.asList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        damages = Arrays.asList(new DamageDice(1, DieType.EIGHT, this.getMainFlavor()));
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
        sb.append("This is a nice, long, shiny scythe.  It's super powerful...");
        sb.append("This can be equipped to: ").append(printWhichSlots());
        // sb.append("And best used if you have these proficiencies:
        // ").append(printWhichTypes());
        // TODO: should this describe that it does 1d6 damage?
        return sb.toString();
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.NECROTIC;
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public Attack modifyAttack(Attack attack) {
        attack = super.modifyAttack(attack).addFlavorAndDamage(this.getMainFlavor(), 100).addToHitBonus(10);
        return attack;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.FINESSE;
    }

}
