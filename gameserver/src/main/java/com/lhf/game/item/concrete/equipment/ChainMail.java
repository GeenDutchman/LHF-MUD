package com.lhf.game.item.concrete.equipment;

import java.util.Collections;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class ChainMail extends Equipable {
    private final int AC = 5;

    public ChainMail() {
        super("Chain Mail", "This is some heavy chainmail. It looks protective... now if only it wasn't so heavy\n");

        this.slots = Collections.singletonList(EquipmentSlots.ARMOR);
        this.types = Collections.singletonList(EquipmentTypes.HEAVYARMOR);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource("AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                        null, "Wearing armor makes you harder to hit", false)
                        .addStatChange(Stats.AC, this.AC));
    }

}
