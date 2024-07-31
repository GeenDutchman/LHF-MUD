package com.lhf.game.item;

import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Item.ItemBuilder;

public enum ItemTemplate {
    HEAL_POTION {
        @Override
        public ItemBuilder applyTemplate(ItemBuilder builder) {
            if (builder == null) {
                builder = new ItemBuilder();
            } else {
                builder.reset();
            }
            builder.setName("Regular Potion of Healing");
            builder.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseOnCreatureEffect(CreatureEffectSource.getCreatureEffectBuilder("Regular Potion Healing")
                        .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, 1)
                                .addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING))));
                return usable;
            });
            return builder;
        }
    },
    GREATER_HEAL_POTION {
        @Override
        public ItemBuilder applyTemplate(ItemBuilder builder) {
            if (builder == null) {
                builder = new ItemBuilder();
            } else {
                builder.reset();
            }
            builder.setName("Regular Potion of Healing");
            builder.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseOnCreatureEffect(CreatureEffectSource.getCreatureEffectBuilder("Regular Potion Healing")
                        .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, 2)
                                .addDamage(new DamageDice(2, DieType.SIX, DamageFlavor.HEALING))));
                return usable;
            });
            return builder;
        }
    },
    SUPERIOR_HEAL_POTION {
        @Override
        public ItemBuilder applyTemplate(ItemBuilder builder) {
            if (builder == null) {
                builder = new ItemBuilder();
            } else {
                builder.reset();
            }
            builder.setName("Regular Potion of Healing");
            builder.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseOnCreatureEffect(CreatureEffectSource.getCreatureEffectBuilder("Regular Potion Healing")
                        .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, 3)
                                .addDamage(new DamageDice(3, DieType.SIX, DamageFlavor.HEALING))));
                return usable;
            });
            return builder;
        }
    },
    CRITICAL_HEAL_POTION {
        @Override
        public ItemBuilder applyTemplate(ItemBuilder builder) {
            if (builder == null) {
                builder = new ItemBuilder();
            } else {
                builder.reset();
            }
            builder.setName("Regular Potion of Healing");
            builder.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseOnCreatureEffect(CreatureEffectSource.getCreatureEffectBuilder("Regular Potion Healing")
                        .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, 3)
                                .addDamage(new DamageDice(3, DieType.EIGHT, DamageFlavor.HEALING))));
                return usable;
            });
            return builder;
        }
    };

    public abstract Item.ItemBuilder applyTemplate(Item.ItemBuilder builder);
}
