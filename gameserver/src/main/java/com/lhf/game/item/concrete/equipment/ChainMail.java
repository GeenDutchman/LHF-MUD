package com.lhf.game.item.concrete.equipment;

import java.util.Collections;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class ChainMail extends Equipable {
    private final int AC = 5;

    public ChainMail(boolean isVisible) {
        super("Chain Mail", isVisible);

        this.slots = Collections.singletonList(EquipmentSlots.ARMOR);
        this.types = Collections.singletonList(EquipmentTypes.HEAVYARMOR);
        this.equipEffects = Collections.singletonList(
                new BasicCreatureEffector(null, this, new EffectPersistence(TickType.CONDITIONAL))
                        .addStatChange(Stats.AC, this.AC));
    }

    @Override
    public String printDescription() {
        return "This is some heavy chainmail. " + "It looks protective... now if only it wasn't so heavy\n";
    }
}
