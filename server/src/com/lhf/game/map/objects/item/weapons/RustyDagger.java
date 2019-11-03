package com.lhf.game.map.objects.item.weapons;

import com.lhf.game.map.objects.item.Item;
import com.lhf.game.map.objects.item.interfaces.EquipType;
import com.lhf.game.map.objects.item.interfaces.Equipable;
import com.lhf.game.map.objects.sharedinterfaces.Examinable;

public class RustyDagger extends Item implements Equipable, Examinable {
    public static EquipType TYPE = EquipType.WEAPON;

    public RustyDagger(String name, boolean isVisible) {
        super(name, isVisible);
    }

    @Override
    public EquipType getType() { return TYPE; }

    @Override
    public String getDescription() {
        return "Rusty Dagger to stab monsters with";
    }
}
