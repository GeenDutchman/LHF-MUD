package com.lhf.game.item.concrete.equipment;

import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.creature.CreatureEffector.BasicCreatureEffector;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

public class Shield extends Equipable {
    private class ACBonus extends BasicCreatureEffector {

        public ACBonus() {
            super(null, Shield.this, new EffectPersistence(TickType.CONDITIONAL));
            this.init();
        }

        public ACBonus(Creature creatureResponsible) {
            super(creatureResponsible, Shield.this, new EffectPersistence(TickType.CONDITIONAL));
            this.init();
        }

        private void init() {
            this.addStatChange(Stats.AC, Shield.this.AC);
        }

    }

    private final int AC = 2;
    private List<EquipmentSlots> slots;
    private List<EquipmentTypes> types;
    private List<CreatureEffector> equippingEffects;

    public Shield(boolean isVisible) {
        super("Shield", isVisible);
        this.types = Collections.singletonList(EquipmentTypes.SHIELD);
        this.slots = Collections.singletonList(EquipmentSlots.SHIELD);
        this.equippingEffects = Collections.singletonList(new ACBonus());
    }

    @Override
    public List<EquipmentTypes> getTypes() {
        return types;
    }

    @Override
    public List<EquipmentSlots> getWhichSlots() {
        return slots;
    }

    @Override
    public List<CreatureEffector> getEquippingEffects(boolean alsoHidden) {
        return this.equippingEffects;
    }

    @Override
    public String printDescription() {
        return "This is a simple shield, it should protect you a little bit. \n";
    }
}
