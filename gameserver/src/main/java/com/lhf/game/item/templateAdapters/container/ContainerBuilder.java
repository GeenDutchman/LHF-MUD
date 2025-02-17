package com.lhf.game.item.templateAdapters.container;

import java.util.Collection;
import java.util.Iterator;

import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DiceD8;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.IItemBuilder;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemContainerCapability;

public final class ContainerBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Container")
            .setDescriptionString("Can contain items.").adjustItemContainerCapability(
                    container -> container != null ? container : ItemContainerCapability.Container.getBuilder());

    public enum ChestDescriptor {
        RANDOM, RUSTY, SHINY, BLUE, SLIPPERY, WOODEN, COLORFUL, METAL, FANCY;

        public static String generateDescription(ChestDescriptor descriptor) {
            if (ChestDescriptor.RANDOM.equals(descriptor)) {
                descriptor = ChestDescriptor.values()[new DiceD8(1).rollDice().getRoll()];
            }
            return descriptor != null ? descriptor.toString().toLowerCase() + " chest" : "nondescript chest";
        }
    }

    public static ContainerBuilder ofChest(ChestDescriptor descriptor) {
        ContainerBuilder builder = new ContainerBuilder();
        builder.setName(ChestDescriptor.generateDescription(descriptor));
        builder.inner.adjustItemContainerCapability(container -> {
            if (container == null) {
                container = ItemContainerCapability.Container.getBuilder();
            }
            container.setItemDepositingAllowed(true).setItemWithdrawalAllowed(true);
            return container;
        });
        return builder;
    }

    public static ContainerBuilder ofDispenser(String name) {
        ContainerBuilder builder = new ContainerBuilder();
        builder.setName(name);
        builder.inner.adjustItemContainerCapability(container -> {
            if (container == null) {
                container = ItemContainerCapability.Container.getBuilder();
            }
            container.setItemDepositingAllowed(false).setItemWithdrawalAllowed(false);
            return container;
        });
        return builder;
    }

    public static ContainerBuilder ofCorpse(ICreature creature, boolean transfer) {
        ContainerBuilder builder = new ContainerBuilder();
        builder.setName(creature != null ? creature.getName() + "'s corpse" : "a corpse");
        builder.inner.setDescriptionString("They are quite clearly dead.  You can't quite tell the cause...");
        builder.inner.adjustItemContainerCapability(container -> {
            if (container == null) {
                container = ItemContainerCapability.Container.getBuilder();
            }
            if (transfer && creature != null) {
                for (Iterator<? extends IItem> it = creature.itemIterator(); it.hasNext();) {
                    IItem item = it.next();
                    it.remove();
                    if (item != null) {
                        container.addItem(item);
                    }
                }
            }
            container.setItemDepositingAllowed(true).setItemWithdrawalAllowed(true);
            return container;
        });
        return builder;
    }

    public ContainerBuilder addProducer(IItemBuilder producer) {
        this.inner.adjustItemContainerCapability(container -> {
            if (container == null) {
                container = ItemContainerCapability.Container.getBuilder();
            }
            container.addItemBuilder(producer);
            return container;
        });
        return this;
    }

    public ContainerBuilder addItem(IItem item) {
        if (item != null) {
            this.inner.adjustItemContainerCapability(container -> {
                if (container == null) {
                    container = ItemContainerCapability.Container.getBuilder();
                }
                container.addItem(item);
                return container;
            });
        }
        return this;
    }

    public ContainerBuilder addItems(Collection<IItem> items) {
        if (items != null) {
            this.inner.adjustItemContainerCapability(container -> {
                if (container == null) {
                    container = ItemContainerCapability.Container.getBuilder();
                }
                for (final IItem iItem : items) {
                    if (iItem == null) {
                        continue;
                    }
                    container.addItem(iItem);
                }
                return container;
            });
        }
        return this;
    }

    @Override
    public ContainerBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public ContainerBuilder setDescriptionString(String descriptionString) {
        this.inner.setDescriptionString(descriptionString);
        return this;
    }

    @Override
    public ContainerBuilder reset() {
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
}
