package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class LeatherArmor extends Equipable {
    private final int AC = 2;

    public LeatherArmor() {
        super("Leather Armor", "This is some simple leather armor. There is only a little blood on it...\n");

        this.slots = Collections.singletonList(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource("Armor AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                        null, "Wearing armor makes you harder to hit", new Deltas().setStatChange(Stats.AC, this.AC)));
    }

    @Override
    public LeatherArmor makeCopy() {
        return this;
    }
}
