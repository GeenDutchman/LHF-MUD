package com.lhf.game.item.templateAdapters.equipable;

import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.EquipableCapability;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;

public final class ShieldBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Shield")
            .setDescriptionString("This is a simple shield, it should protect you a little bit.")
            .adjustEquipableCapability(equipable -> equipable != null
                    ? equipable.addEquipmentSlot(EquipmentSlots.SHIELD).addEquipmentType(EquipmentTypes.SHIELD)
                    : new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.SHIELD)
                            .addEquipmentType(EquipmentTypes.SHIELD))
            .setTakeable(true);

    public final static String EQUIP_EFFECT_SOURCE_NAME = "Shield AC Boost";
    public final static String EQUIP_EFFECT_DESCRIPTION = "Using a shield makes you harder to hit";

    private static CreatureEffectSource.Builder buildACEffect(int acValue) {
        return new CreatureEffectSource.Builder(EQUIP_EFFECT_SOURCE_NAME)
                .setPersistence(new EffectPersistence(TickType.CONDITIONAL)).setDescription(EQUIP_EFFECT_DESCRIPTION)
                .setOnApplication(new Deltas().setStatChange(Stats.AC, acValue));
    }

    public static ShieldBuilder ofWood() {
        ShieldBuilder builder = new ShieldBuilder();
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.SHIELD);
            }
            equipable.addEquipmentSlot(EquipmentSlots.SHIELD).addEquipmentType(EquipmentTypes.SHIELD);
            equipable.addEquipEffect(ShieldBuilder.buildACEffect(2));
            return equipable;
        });
        return builder;
    }

    @Override
    public IItem build() {
        return this.inner.build();
    }

    @Override
    public Item.ItemBuilder getItemBuilder() {
        return this.inner;
    }

    @Override
    public ShieldBuilder reset() {
        this.inner.reset();
        return this;
    }

    @Override
    public ShieldBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public ShieldBuilder setDescriptionString(String descriptionString) {
        this.inner.setDescriptionString(descriptionString);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ShieldBuilder [inner=").append(inner).append("]");
        return builder.toString();
    }

}
