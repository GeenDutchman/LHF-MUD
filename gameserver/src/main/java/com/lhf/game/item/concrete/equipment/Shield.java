package com.lhf.game.item.concrete.equipment;

import java.util.Collections;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;

public class Shield extends Equipable {

    private final int AC = 2;

    public Shield() {
        super("Shield", "This is a simple shield, it should protect you a little bit. \n");
        this.types = Collections.singletonList(EquipmentTypes.SHIELD);
        this.slots = Collections.singletonList(EquipmentSlots.SHIELD);
        this.equipEffects = Collections.singletonList(
                new CreatureEffectSource.Builder("Shield AC Boost")
                        .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                        .setDescription("Using a shield makes you harder to hit")
                        .setOnApplication(new Deltas().setStatChange(Stats.AC, this.AC)).build());
    }

    @Override
    public Shield makeCopy() {
        return this;
    }
}
