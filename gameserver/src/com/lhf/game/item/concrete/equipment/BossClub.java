package com.lhf.game.item.concrete.equipment;

import com.lhf.game.dice.*;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;

import java.util.*;

public class BossClub extends Weapon {

    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<DamageDice> damages;
    private Map<String, Integer> equippingChanges;

    public BossClub(boolean isVisible) {
        super("Boss Club", isVisible);

        slots = Collections.singletonList(EquipmentSlots.WEAPON);
        types = Arrays.asList(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD);
        damages = Arrays.asList(new DamageDice(2, DieType.EIGHT, this.getMainFlavor()));
        equippingChanges = new HashMap<>(0);

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
        return "This is a large club, it seems a bit rusty... wait thats not rust...\n" +
                this.printStats();
    }

    @Override
    public DamageFlavor getMainFlavor() {
        return DamageFlavor.BLUDGEONING;
    }

    @Override
    public List<DamageDice> getDamages() {
        return this.damages;
    }

    @Override
    public WeaponSubtype getSubType() {
        return WeaponSubtype.MARTIAL;
    }

}
