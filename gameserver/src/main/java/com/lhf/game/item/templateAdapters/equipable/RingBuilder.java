package com.lhf.game.item.templateAdapters.equipable;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.EquipableCapability;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.IItemBuilder;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.game.item.Item;
import com.lhf.game.item.UsableCapability;

public final class RingBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Ring")
            .setDescriptionString("It is a ring that goes on the fingers of either hand")
            .adjustEquipableCapability(equipable -> equipable != null
                    ? equipable.addEquipmentSlot(EquipmentSlots.RIGHTHAND).addEquipmentSlot(EquipmentSlots.LEFTHAND)
                    : new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.LEFTHAND)
                            .addEquipmentSlot(EquipmentSlots.RIGHTHAND));

    public static RingBuilder ofSeeing() {
        RingBuilder builder = new RingBuilder();
        builder.inner.setDescriptionString("This ring can help you see things that are not visible to the naked eye. ");
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.LEFTHAND);
            }
            equipable.addEquipmentSlot(EquipmentSlots.RIGHTHAND).addEquipmentSlot(EquipmentSlots.LEFTHAND);
            equipable.addEquipEffect(new CreatureEffectSource.Builder("Seeing wisdom")
                    .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                    .setDescription("If you can see, then you are wise.")
                    .setOnApplication(new Deltas().setAttributeBonusChange(Attributes.WIS, 2)));
            return equipable;
        });
        builder.inner.adjustUsableCapability(usable -> {
            if (usable == null) {
                usable = new UsableCapability.Usable.UsableBuilder();
            }
            usable.setEquippingRequired(true).setSelfOnly(false).setTotalNumberUsableTimes(3);
            usable.addUseOnAreaEffect(new RoomEffectSource.Builder("Seeing Everything").instantPersistence()
                    .setShowHidden(RoomEffectSource.ShowHidden.TO_USER_ONLY));
            return usable;
        });
        return builder;
    }

    @Override
    public RingBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public RingBuilder reset() {
        this.inner.reset();
        return this;
    }

    @Override
    public Item.ItemBuilder getItemBuilder() {
        return this.inner;
    }

    @Override
    public IItem build() {
        return this.inner.build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RingBuilder [inner=").append(inner).append("]");
        return builder.toString();
    }

}
