package com.lhf.game.item.concrete.equipment;

import java.util.Collections;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class ChainMail extends Equipable {
    private final int AC = 5;

    public ChainMail(boolean isVisible) {
        super("Chain Mail", isVisible);

        this.slots = Collections.singletonList(EquipmentSlots.ARMOR);
        this.types = Collections.singletonList(EquipmentTypes.HEAVYARMOR);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource("AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                        null, "Wearing armor makes you harder to hit", false)
                        .addStatChange(Stats.AC, this.AC));
    }

    @Override
    public String printDescription() {
        return "This is some heavy chainmail. " + "It looks protective... now if only it wasn't so heavy\n";
    }
}
