package com.lhf.game.item.concrete.equipment;

import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.EquipableHiddenEffect;

public class MantleOfDeath extends EquipableHiddenEffect {
    private final int AC = 10;
    private final int MAX_HEALTH = 100;

    public MantleOfDeath() {
        super("Mantle Of Death",
                "This fearsome hooded robe seems a little bit overpowered to be in your puny hands. \n");
        this.setVisible(false);

        this.slots = List.of(EquipmentSlots.ARMOR);
        this.types = List.of(EquipmentTypes.LIGHTARMOR, EquipmentTypes.LEATHER);
        this.hiddenEquipEffects = List
                .of(new CreatureEffectSource.Builder("AC Boost")
                        .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                        .setDescription("Wearing armor makes you harder to hit")
                        .setOnApplication(new Deltas().setStatChange(Stats.AC, this.AC)
                                .setStatChange(Stats.MAXHP, this.MAX_HEALTH)
                                .setStatChange(Stats.CURRENTHP, this.MAX_HEALTH))
                        .build());
    }

    @Override
    public MantleOfDeath makeCopy() {
        return this;
    }
}
