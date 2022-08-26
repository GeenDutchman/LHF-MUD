package com.lhf.game.item.concrete.equipment;

import java.util.Collections;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class Shield extends Equipable {

    private final int AC = 2;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        this.types = Collections.singletonList(EquipmentTypes.SHIELD);
        this.slots = Collections.singletonList(EquipmentSlots.SHIELD);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource("AC Boost", new EffectPersistence(TickType.CONDITIONAL),
                        null, "Using a shield makes you harder to hit", false)
                        .addStatChange(Stats.AC, this.AC));
    }

    @Override
    public String printDescription() {
        return "This is a simple shield, it should protect you a little bit. \n";
    }
}
