package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

public class Equipable extends Usable {
    protected List<EquipmentTypes> types;
    protected List<EquipmentSlots> slots;
    protected List<CreatureEffect> equipEffects;
    protected List<CreatureEffect> hiddenEquipEffects;

    private void initLists() {
        this.types = new ArrayList<>();
        this.slots = new ArrayList<>();
        this.equipEffects = new ArrayList<>();
        this.hiddenEquipEffects = new ArrayList<>();
    }

    public Equipable(String name, boolean isVisible) {
        super(name, isVisible, -1);
        this.initLists();
    }

    public Equipable(String name, boolean isVisible, int useSoManyTimes) {
        super(name, isVisible, useSoManyTimes);
        this.initLists();
    }

    // returns unmodifiable
    public List<EquipmentTypes> getTypes() {
        return Collections.unmodifiableList(this.types);
    }

    // returns unmodifiable
    public List<EquipmentSlots> getWhichSlots() {
        return Collections.unmodifiableList(this.slots);
    }

    // returns unmodifiable
    public List<CreatureEffect> getEquippingEffects(boolean alsoHidden) {
        List<CreatureEffect> comboList = new ArrayList<>(this.equipEffects);
        if (alsoHidden) {
            comboList.addAll(this.hiddenEquipEffects);
        }
        return Collections.unmodifiableList(comboList);
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = super.produceMessage();
        for (CreatureEffect effector : this.getEquippingEffects(false)) {
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
        for (CreatureEffect effector : this.getEquippingEffects(true)) {
            equipper.sendMsg(equipper.applyEffect(effector));
        }
    }

    public void onUnequippedBy(Creature unequipper) {
        for (CreatureEffect effector : this.getEquippingEffects(true)) {
            if (effector.getPersistence() != null
                    && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                unequipper.sendMsg(unequipper.applyEffect(effector, true));
            }
        }
    }

}
