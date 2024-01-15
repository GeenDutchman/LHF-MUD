package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class MantleOfDeath extends Equipable {
    private final int AC = 10;
    private final int MAX_HEALTH = 100;

    public MantleOfDeath(boolean isVisible) {
        super("Mantle Of Death", isVisible);

        this.slots = List.of(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.equipEffects = List.of(new CreatureEffectSource("AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                null, "Wearing armor makes you harder to hit", false)
                .addStatChange(Stats.AC, this.AC).addStatChange(Stats.MAXHP, this.MAX_HEALTH)
                .addStatChange(Stats.CURRENTHP, this.MAX_HEALTH));
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(
                "This fearsome hooded robe seems a little bit overpowered to be in your puny hands. \n");
        return sb.toString();
    }

    @Override
    public MantleOfDeath makeCopy() {
        return new MantleOfDeath(this.isVisible());
    }
}
