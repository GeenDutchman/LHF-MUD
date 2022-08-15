package com.lhf.game.item.interfaces;

import java.util.List;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public abstract class Equipable extends Usable {
    public Equipable(String name, boolean isVisible) {
        super(name, isVisible, -1);
    }

    public Equipable(String name, boolean isVisible, int useSoManyTimes) {
        super(name, isVisible, useSoManyTimes);
    }

    public abstract List<EquipmentTypes> getTypes();

    public abstract List<EquipmentSlots> getWhichSlots();

    public abstract List<CreatureEffector> getEquippingEffects(boolean alsoHidden);

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        for (CreatureEffector effector : this.getEquippingEffects(false)) {
            seeOutMessage.addEffector(effector);
        }
        if (this.getWhichSlots() != null && this.getWhichSlots().size() > 0) {
            for (EquipmentSlots slot : this.getWhichSlots()) {
                seeOutMessage.addSeen(SeeCategory.EQUIPMENT_SLOTS, slot);
            }
        }
        if (this.getTypes() != null && this.getTypes().size() > 0) {
            for (EquipmentTypes type : this.getTypes()) {
                seeOutMessage.addSeen(SeeCategory.PROFICIENCIES, type);
            }
        }
        return seeOutMessage;
    }

    public void onEquippedBy(Creature equipper) {
        for (CreatureEffector effector : this.getEquippingEffects(true)) {
            equipper.sendMsg(equipper.applyAffects(effector));
        }
    }

    public void onUnequippedBy(Creature unequipper) {
        for (CreatureEffector effector : this.getEquippingEffects(true)) {
            if (effector.getPersistence() != null
                    && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                unequipper.sendMsg(unequipper.applyAffects(effector, true));
            }
        }
    }

}
