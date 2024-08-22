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
import com.lhf.game.item.IItem.IItemBuilder;
import com.lhf.game.item.IItem.IItemBuilderAdapter;
import com.lhf.game.item.Item;
import com.lhf.game.item.RenameCapability;

public final class ArmorBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Armor")
            .setDescriptionString("If you choose to wear it, most Armor can protect you from harm.")
            .adjustEquipableCapability(equipable -> equipable != null ? equipable.addEquipmentSlot(EquipmentSlots.ARMOR)
                    : new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.ARMOR))
            .setTakeable(true);

    public final static String EQUIP_EFFECT_SOURCE_NAME = "Armor AC Boost";
    public final static String EQUIP_EFFECT_DESCRIPTION = "Wearing armor makes you harder to hit";

    private static CreatureEffectSource.Builder buildACEffect(int acValue) {
        return new CreatureEffectSource.Builder(EQUIP_EFFECT_SOURCE_NAME)
                .setPersistence(new EffectPersistence(TickType.CONDITIONAL)).setDescription(EQUIP_EFFECT_DESCRIPTION)
                .setOnApplication(new Deltas().setStatChange(Stats.AC, acValue));
    }

    public static ArmorBuilder asChainMail() {
        ArmorBuilder builder = new ArmorBuilder();
        builder.inner.setName("Chain Mail").setDescriptionString(
                "This is some heavy chainmail. It looks protective... now if only it wasn't so heavy");
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.ARMOR);
            }
            equipable.addEquipmentType(EquipmentTypes.HEAVYARMOR);
            equipable.addEquipEffect(ArmorBuilder.buildACEffect(3));
            return equipable;
        });
        return builder;
    }

    public static ArmorBuilder asLeatherArmor() {
        ArmorBuilder builder = new ArmorBuilder();
        builder.inner.setName("Leather Armor")
                .setDescriptionString("This is some simple leather armor. There is only a little blood on it...");
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.ARMOR);
            }
            equipable.addEquipmentType(EquipmentTypes.LIGHTARMOR).addEquipmentType(EquipmentTypes.LEATHER);
            equipable.addEquipEffect(ArmorBuilder.buildACEffect(2));
            return equipable;
        });
        return builder;
    }

    public static ArmorBuilder asMantleOfDeath() {
        ArmorBuilder builder = new ArmorBuilder();
        builder.inner.setVisible(false);
        builder.inner.setName("Mantle of Death").setDescriptionString(
                "This fearsome hooded robe seems a little bit overpowered to be in your puny hands.");
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.ARMOR);
            }
            equipable.addEquipmentType(EquipmentTypes.LIGHTARMOR).addEquipmentType(EquipmentTypes.LEATHER);
            equipable.addEquipEffect(ArmorBuilder.buildACEffect(100));
            equipable.addHiddenEquipEffect(
                    CreatureEffectSource.getCreatureEffectBuilder("The Mantle of Death protects you from Death")
                            .setPersistence(new EffectPersistence(TickType.CONDITIONAL)).setOnApplication(
                                    new Deltas().setStatChange(Stats.MAXHP, 100).setStatChange(Stats.CURRENTHP, 100)));
            return equipable;
        });
        return builder;
    }

    public static ArmorBuilder asCarnivorousArmor() {
        ArmorBuilder builder = new ArmorBuilder();
        builder.inner.setName("Carnivorous Armor")
                .setDescriptionString("This is some simple leather armor. There is plenty of blood on it...");
        builder.inner.adjustRenameableCapability(renamable -> renamable != null ? renamable.addName("Leather Armor")
                : new RenameCapability.Renameable().addName("Leather Armor"));
        builder.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.ARMOR);
            }
            equipable.addEquipmentType(EquipmentTypes.LIGHTARMOR).addEquipmentType(EquipmentTypes.LEATHER);
            equipable.addEquipEffect(ArmorBuilder.buildACEffect(2));
            return equipable;
        });

        /*
         * TODO: changes on useage
         * 
         * self only usage, needs to be equipped
         * 
         * if has been equipped and used: -> message
         * "The Carnivorous armor snuggles around you as you poke at it, but otherwise does nothing"
         * 
         * if user/equipOwner is @{see
         * com.lhf.game.enums.HealthBuckets.CRITICALLY_INJURED CRITICALLY_INJURED} or
         * worse -> message "You need more health to use this item."
         * 
         * else -> health changed to critical/to 5hp with description
         * "You are eaten alive...just a bite." instant application Delta -> message
         * "A thousand teeth sink into your body and you feel life force ripped out of you. "
         * ->->
         * "Once it is sated, you feel the Carnivorous armor tighten up around its most recent, precious meal."
         * ->-> "It leave shte rest for later." -> add 3 to user Stats.AC
         * "Protect the meal"
         */

        /*
         * If used, and then unequipped, eats health to critical/5hp
         */

        return builder;

    }

    @Override
    public IItemBuilderAdapter setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public ArmorBuilder reset() {
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
        builder.append("ArmorBuilder [inner=").append(inner).append("]");
        return builder.toString();
    }

}
