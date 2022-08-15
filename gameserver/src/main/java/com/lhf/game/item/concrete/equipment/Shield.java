package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class Shield extends Equipable {

    private final int AC = 2;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        this.types = Collections.singletonList(EquipmentTypes.SHIELD);
        this.slots = Collections.singletonList(EquipmentSlots.SHIELD);
        this.equipEffects = Collections
                .singletonList(new BasicCreatureEffector(null, this, new EffectPersistence(TickType.CONDITIONAL))
                        .addStatChange(Stats.AC, this.AC));
    }

    @Override
    public String printDescription() {
        return "This is a simple shield, it should protect you a little bit. \n";
    }
}
