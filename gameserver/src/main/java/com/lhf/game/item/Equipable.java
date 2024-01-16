package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lhf.game.TickType;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Equipable extends Usable {
    protected List<EquipmentTypes> types;
    protected List<EquipmentSlots> slots;
    protected List<CreatureEffectSource> equipEffects;

    private void initLists() {
        this.types = new ArrayList<>();
        this.slots = new ArrayList<>();
        this.equipEffects = new ArrayList<>();
    }

    public Equipable(String name, String description) {
        super(name, description, -1, null, null);
        this.initLists();
    }

    public Equipable(String name, String description, int useSoManyTimes) {
        super(name, description, useSoManyTimes, null, null);
        this.initLists();
    }

    public Equipable(String name, String description, int useSoManyTimes, CreatureVisitor creatureVisitor,
            ItemVisitor itemVisitor) {
        super(name, description, useSoManyTimes, creatureVisitor, itemVisitor);
        this.initLists();
    }

    protected void copyOverwriteTo(Equipable other) {
        other.types = new ArrayList<>(this.types);
        other.slots = new ArrayList<>(this.slots);
        other.equipEffects = new ArrayList<>();
        for (final CreatureEffectSource source : this.equipEffects) {
            other.equipEffects.add(source.makeCopy());
        }
        super.copyOverwriteTo(other);
    }

    @Override
    public Equipable makeCopy() {
        Equipable equipable = new Equipable(this.getName(), this.descriptionString, this.numCanUseTimes,
                this.creatureVisitor, this.itemVisitor);
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
    public List<CreatureEffectSource> getEquippingEffects() {
        return Collections.unmodifiableList(this.equipEffects);
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = (SeeEvent.Builder) super.produceMessage().copyBuilder();
        for (CreatureEffectSource effector : this.getEquippingEffects()) {
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
        for (CreatureEffectSource effector : this.getEquippingEffects()) {
            ICreature.eventAccepter.accept(equipper,
                    equipper.applyEffect(new CreatureEffect(effector, equipper, this)));
        }
    }

    public void onUnequippedBy(ICreature unequipper) {
        for (CreatureEffectSource effector : this.getEquippingEffects()) {
            if (effector.getPersistence() != null
                    && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                ICreature.eventAccepter.accept(unequipper,
                        unequipper.applyEffect(new CreatureEffect(effector, unequipper, this), true));
            }
        }
    }

}
