package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.map.Area;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
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
        super(name, description, -1, null);
        this.initLists();
    }

    public Equipable(String name, String description, int useSoManyTimes) {
        super(name, description, useSoManyTimes, null);
        this.initLists();
    }

    public Equipable(String name, String description, int useSoManyTimes,
            Set<CreatureEffectSource> useOnCreatureEffects) {
        super(name, description, useSoManyTimes, useOnCreatureEffects);
        this.initLists();
    }

    protected Equipable(Equipable other) {
        this(other.getName(), other.descriptionString, other.numCanUseTimes, other.creatureUseEffects);
        this.types.addAll(other.types);
        this.slots.addAll(other.slots);
        for (final CreatureEffectSource source : other.equipEffects) {
            this.equipEffects.add(source);
        }
    }

    @Override
    public Equipable makeCopy() {
        if (this.numCanUseTimes < 0) {
            return this;
        }
        return new Equipable(this);
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
        return this.equipEffects != null ? Collections.unmodifiableList(this.equipEffects) : List.of();
    }

    @Override
    public boolean useOn(CommandContext ctx, ICreature creature) {
        if (!ctx.getCreature().hasItem(this)) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).setTarget(creature);
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, creature);
    }

    @Override
    public boolean useOn(CommandContext ctx, IItem item) {
        if (!ctx.getCreature().hasItem(this)) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).setTarget(item);
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, item);
    }

    @Override
    public boolean useOn(CommandContext ctx, Area area) {
        if (!ctx.getCreature().hasItem(this)) {
            ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                    .setUsable(this).setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).setTarget(area);
            ctx.receive(useOutMessage);
            return false;
        }
        return super.useOn(ctx, area);
    }

    @Override
    public SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = (SeeEvent.Builder) super.produceMessage(seeOutMessage).copyBuilder();
        }
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
                        unequipper.repealEffect(effector.getName()));
            }
        }
    }

}
