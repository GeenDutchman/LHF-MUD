package com.lhf.game.item.templateAdapters.potion;

import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.enums.HealType;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.game.item.UsableCapability;
import com.lhf.server.interfaces.NotNull;

public final class PotionBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Potion").setDescriptionString("A potion.")
            .adjustUsableCapability(usable -> usable != null ? usable : new UsableCapability.Usable.UsableBuilder());

    /**
     * Creates an item after the template of a healing potion. They are, in order of
     * potency, Regular, Greater, Superior, and Critical.
     * 
     * @param type
     * @see HealType
     * @return ItemBuilder after the template
     */
    public PotionBuilder healPotionTemplate(@NotNull final HealType type) {
        final String name = String.format("%s Potion of Healing", type);
        this.inner.setName(name);
        inner.adjustUsableCapability(usable -> {
            if (usable == null) {
                usable = UsableCapability.Usable.getBuilder();
            }
            usable.addUseOnCreatureEffect(
                    CreatureEffectSource.getCreatureEffectBuilder(name).instantPersistence()
                            .setOnApplication(new Deltas()
                                    .setStatChange(Stats.CURRENTHP,
                                            type != null ? type.produceStaticBonus()
                                                    : HealType.Regular.produceStaticBonus())
                                    .addDamage(type != null ? type.produceDamageType()
                                            : HealType.Regular.produceDamageType())));
            return usable;
        });

        return this;
    }

    public PotionBuilder addUseEffect(CreatureEffectSource.Builder builder) {
        if (builder != null) {
            inner.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseOnCreatureEffect(builder);
                return usable;
            });
        }
        return this;
    }

    @Override
    public PotionBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public PotionBuilder setDescriptionString(String descriptionString) {
        this.inner.setDescriptionString(descriptionString);
        return this;
    }

    @Override
    public PotionBuilder reset() {
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
        builder.append("PotionBuilder [inner=").append(inner).append("]");
        return builder.toString();
    }

}
