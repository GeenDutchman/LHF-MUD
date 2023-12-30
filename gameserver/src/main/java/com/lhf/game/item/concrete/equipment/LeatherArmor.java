package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class LeatherArmor extends Equipable {
    private final int AC = 2;

    public LeatherArmor(boolean isVisible) {
        super("Leather Armor", isVisible);

        this.slots = Collections.singletonList(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource("AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                        null, "Wearing armor makes you harder to hit", false)
                        .addStatChange(Stats.AC, this.AC));
    }

    @Override
    public String printDescription() {
        return "This is some simple leather armor. " + "There is only a little blood on it...\n";
    }

    @Override
    public LeatherArmor makeCopy() {
        return new LeatherArmor(this.checkVisibility());
    }
}
