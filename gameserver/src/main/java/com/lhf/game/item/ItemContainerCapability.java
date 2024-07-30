package com.lhf.game.item;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.IItem.IItemBuilder;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface ItemContainerCapability extends ItemCapability, ItemContainer {

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.CONTAINER;
    }

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        return;
    }

    @Override
    default String getName() {
        return ItemCapabilityNames.CONTAINER.toString();
    }

    @Override
    default String getDescription() {
        return "This item can contain other items.";
    }

    public IItem removeOne();

    public boolean isItemDepositingAllowed();

    public boolean isItemWithdrawalAllowed();

    public static ItemContainerCapability generateItemContainerCapability() {
        return new Container(new Container.Builder());
    }

    public static final class Container implements ItemContainerCapability {
        private final LinkedList<IItem> items;
        private final IItemBuilder producer;
        private final boolean itemDepositingAllowed;
        private final boolean itemWithdrawalAllowed;

        public static final class Builder {
            private List<IItemBuilder> itemBuilders;
            private List<IItem> items;
            private IItemBuilder producer;
            private boolean itemDepositingAllowed;
            private boolean itemWithdrawalAllowed;

            public List<IItemBuilder> getItemBuilders() {
                return itemBuilders;
            }

            public Builder setItemBuilders(List<IItemBuilder> itemBuilders) {
                this.itemBuilders = itemBuilders;
                return this;
            }

            public Builder addItemBuilder(IItemBuilder itemBuilder) {
                if (itemBuilder != null) {
                    if (this.itemBuilders == null) {
                        this.itemBuilders = new LinkedList<>();
                    }
                    this.itemBuilders.add(itemBuilder);
                }
                return this;
            }

            public Builder adjustItemBuilders(Consumer<List<IItemBuilder>> adjustor) {
                if (adjustor != null) {
                    if (this.itemBuilders == null) {
                        this.itemBuilders = new LinkedList<>();
                    }
                    adjustor.accept(this.itemBuilders);
                }
                return this;
            }

            public Builder clearItemBuilders() {
                if (this.itemBuilders != null) {
                    this.itemBuilders.clear();
                }
                return this;
            }

            public List<IItem> getItems() {
                return items;
            }

            public Builder setItems(List<IItem> items) {
                this.items = items;
                return this;
            }

            public Builder addItem(IItem item) {
                if (item != null) {
                    if (this.items == null) {
                        this.items = new LinkedList<>();
                    }
                    this.items.add(item);
                }
                return this;
            }

            public Builder adjustItems(Consumer<List<IItem>> adjustor) {
                if (adjustor != null) {
                    if (this.items == null) {
                        this.items = new LinkedList<>();
                    }
                    adjustor.accept(items);
                }
                return this;
            }

            public Builder clearItems() {
                if (this.items != null) {
                    this.items.clear();
                }
                return this;
            }

            public IItemBuilder getProducer() {
                return producer;
            }

            public Builder setProducer(IItemBuilder producer) {
                this.producer = producer;
                return this;
            }

            public LinkedList<IItem> buildItemsList() {
                LinkedList<IItem> made = new LinkedList<>();
                if (this.items != null) {
                    for (IItem item : this.items) {
                        if (item == null) {
                            continue;
                        }
                        made.add(item.makeCopy());
                    }
                }
                if (this.itemBuilders != null) {
                    for (IItemBuilder builder : this.itemBuilders) {
                        if (builder == null) {
                            continue;
                        }
                        made.add(builder.build());
                    }
                }
                return made;
            }

            public boolean isItemDepositingAllowed() {
                return itemDepositingAllowed;
            }

            public void setItemDepositingAllowed(boolean itemDepositingAllowed) {
                this.itemDepositingAllowed = itemDepositingAllowed;
            }

            public boolean isItemWithdrawalAllowed() {
                return itemWithdrawalAllowed;
            }

            public void setItemWithdrawalAllowed(boolean itemWithdrawalAllowed) {
                this.itemWithdrawalAllowed = itemWithdrawalAllowed;
            }

            public Container build() {
                return new Container(this);
            }

        }

        public static Builder getBuilder() {
            return new Builder();
        }

        private Container(Builder builder) {
            if (builder == null) {
                this.items = new LinkedList<>();
                this.producer = null;
                this.itemDepositingAllowed = false;
                this.itemWithdrawalAllowed = false;
            } else {
                this.items = builder.buildItemsList();
                this.producer = builder.getProducer();
                this.itemDepositingAllowed = builder.isItemDepositingAllowed();
                this.itemWithdrawalAllowed = builder.isItemWithdrawalAllowed();
            }
        }

        @Override
        public boolean isItemDepositingAllowed() {
            return this.itemDepositingAllowed;
        }

        @Override
        public boolean isItemWithdrawalAllowed() {
            return this.itemWithdrawalAllowed;
        }

        @Override
        public IItem removeOne() {
            if (this.items != null && !this.items.isEmpty()) {
                return this.items.pollFirst();
            }
            if (this.producer != null) {
                return this.producer.build();
            }
            return null;
        }

        @Override
        public Collection<IItem> getItems() {
            return this.items != null ? Collections.unmodifiableList(this.items) : List.of();
        }

        @Override
        public boolean addItem(IItem item) {
            if (item != null) {
                return this.items.add(item);
            }
            return false;
        }

        @Override
        public Optional<IItem> removeItem(String name) {
            if (!this.isItemWithdrawalAllowed()) {
                return Optional.empty();
            }
            for (final Iterator<IItem> iterator = this.items.iterator(); iterator.hasNext();) {
                final IItem item = iterator.next();
                if (item == null) {
                    iterator.remove();
                    continue;
                }
                if (item.CheckNameRegex(name, 3)) {
                    iterator.remove();
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean removeItem(IItem item) {
            return this.items.remove(item);
        }

        @Override
        public Iterator<? extends IItem> itemIterator() {
            return this.items.iterator();
        }

    }
}
