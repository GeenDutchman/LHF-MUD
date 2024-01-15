package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Equipable extends Usable {
    protected List<EquipmentTypes> types;
    protected List<EquipmentSlots> slots;
    protected List<CreatureEffectSource> equipEffects;
    protected List<CreatureEffectSource> hiddenEquipEffects;

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

    protected void copyOverwriteTo(Equipable other) {
        other.types = new ArrayList<>(this.types);
        other.slots = new ArrayList<>(this.slots);
        other.equipEffects = new ArrayList<>();
        for (final CreatureEffectSource source : this.equipEffects) {
            other.equipEffects.add(source.makeCopy());
        }
        for (final CreatureEffectSource source : this.hiddenEquipEffects) {
            other.hiddenEquipEffects.add(source.makeCopy());
        }
        super.copyOverwriteTo(other);
    }

    @Override
    public Equipable makeCopy() {
        Equipable equipable = new Equipable(this.getName(), this.isVisible(), this.numCanUseTimes);
        this.copyOverwriteTo(equipable);
        return equipable;
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        visitor.visit(this);
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
    public List<CreatureEffectSource> getEquippingEffects(boolean alsoHidden) {
        List<CreatureEffectSource> comboList = new ArrayList<>(this.equipEffects);
        if (alsoHidden) {
            comboList.addAll(this.hiddenEquipEffects);
        }
        return Collections.unmodifiableList(comboList);
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = (SeeEvent.Builder) super.produceMessage().copyBuilder();
        for (CreatureEffectSource effector : this.getEquippingEffects(false)) {
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
        return seeOutMessage.Build();
    }

    public void onEquippedBy(ICreature equipper) {
        for (CreatureEffectSource effector : this.getEquippingEffects(true)) {
            ICreature.eventAccepter.accept(equipper,
                    equipper.applyEffect(new CreatureEffect(effector, equipper, this)));
        }
    }

    public void onUnequippedBy(ICreature unequipper) {
        for (CreatureEffectSource effector : this.getEquippingEffects(true)) {
            if (effector.getPersistence() != null
                    && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                ICreature.eventAccepter.accept(unequipper,
                        unequipper.applyEffect(new CreatureEffect(effector, unequipper, this), true));
            }
        }
    }

}
