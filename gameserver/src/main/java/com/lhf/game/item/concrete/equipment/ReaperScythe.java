package com.lhf.game.item.concrete.equipment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.battle.Attack;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

public class ReaperScythe extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;
    private Map<String, Integer> equippingChanges;

    public ReaperScythe(boolean isVisible) {
        super("Reaper Scythe", isVisible);

        slots = Arrays.asList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        damages = Arrays.asList(new DamageDice(1, DieType.EIGHT, this.getMainFlavor()));
        equippingChanges = new HashMap<>(0); // changes nothing
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
    public String printDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("This is a nice, long, shiny scythe.  It's super powerful...\n");
        sb.append(this.printStats());
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
        attack = super.modifyAttack(attack).addToHitBonus(10);
        attack.addDamageBonus(100);
        return attack;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.FINESSE;
    }

}
