package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.ItemCapability;
import com.lhf.game.item.ItemCapability.ItemCapabilityNames;
import com.lhf.game.item.ItemVisitor;
import com.lhf.game.item.RenameCapability;
import com.lhf.server.interfaces.NotNull;

public interface ItemContainer extends Examinable {

    /**
     * This returns an immutable Collection of Items
     * 
     * @return Immutable collection
     */
    public abstract Collection<IItem> getItems();

    public abstract boolean addItem(IItem item);

    public abstract Optional<IItem> removeItem(String name);

    public abstract boolean removeItem(IItem item);

    public abstract Iterator<? extends IItem> itemIterator();

    public static boolean transfer(ItemContainer from, ItemContainer to, Predicate<IItem> predicate, boolean copyItem) {
        if (from == null || to == null) {
            return false;
        }
        boolean changed = false;
        for (Iterator<? extends IItem> it = from.itemIterator(); it.hasNext();) {
            IItem item = it.next();
            if (item != null && (predicate != null ? predicate.test(item) : true)
                    && (copyItem ? to.addItem(item.makeCopy()) : to.addItem(item))) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    public default void acceptItemVisitor(ItemVisitor visitor) {
        if (visitor == null) {
            return;
        }
        for (IItem item : this.getItems()) {
            if (item == null) {
                continue;
            }
            item.acceptItemVisitor(visitor);
        }
    }

    public static final class ItemFilterQuery implements Predicate<IItem> {
        private String objectName;
        private boolean searchNameUseRegex = false;
        private EnumMap<ItemCapability.ItemCapabilityNames, Boolean> capabilityChecks = new EnumMap<>(
                ItemCapability.ItemCapabilityNames.class);
        private Boolean visible = true;
        private boolean checkMainNameOnly = false;
        private String toStringRegex;

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex) {
            this.objectName = objectName;
            this.searchNameUseRegex = searchNameUseRegex;
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex,
                EnumMap<ItemCapabilityNames, Boolean> capabilities) {
            this.objectName = objectName;
            this.searchNameUseRegex = searchNameUseRegex;
            this.capabilityChecks = capabilities != null ? new EnumMap<>(capabilities) : null;
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex,
                EnumMap<ItemCapabilityNames, Boolean> capabilities, Boolean visible, boolean checkMainNameOnly,
                String toStringRegex) {
            this.objectName = objectName;
            this.searchNameUseRegex = searchNameUseRegex;
            this.capabilityChecks = capabilities != null ? new EnumMap<>(capabilities) : null;
            this.visible = visible;
            this.checkMainNameOnly = checkMainNameOnly;
            this.toStringRegex = toStringRegex;
        }

        public ItemFilterQuery setSearchName(String searchName) {
            this.objectName = searchName;
            this.searchNameUseRegex = false;
            return this;
        }

        public ItemFilterQuery setSearchNameRegex(String searchName) {
            this.objectName = searchName;
            this.searchNameUseRegex = true;
            return this;
        }

        public ItemFilterQuery onlySearchMainName() {
            this.checkMainNameOnly = true;
            return this;
        }

        public ItemFilterQuery searchAlsoRenames() {
            this.checkMainNameOnly = false;
            return this;
        }

        public ItemFilterQuery searchOnlyVisible() {
            this.visible = true;
            return this;
        }

        public ItemFilterQuery searchOnlyInvisible() {
            this.visible = false;
            return this;
        }

        public ItemFilterQuery ignoreVisibility() {
            this.visible = null;
            return this;
        }

        public ItemFilterQuery needsCapability(@NotNull ItemCapability.ItemCapabilityNames capability) {
            this.capabilityChecks.put(capability, true);
            return this;
        }

        public ItemFilterQuery forbiddenCapability(@NotNull ItemCapability.ItemCapabilityNames capability) {
            this.capabilityChecks.put(capability, false);
            return this;
        }

        public ItemFilterQuery ignoreCapability(@NotNull ItemCapability.ItemCapabilityNames capability) {
            this.capabilityChecks.remove(capability);
            return this;
        }

        public ItemFilterQuery setToStringRegex(String regex) {
            this.toStringRegex = regex;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ItemFilterQuery [objectName=").append(objectName).append(", searchNameUseRegex=")
                    .append(searchNameUseRegex).append(", capabilityChecks=").append(capabilityChecks)
                    .append(", visible=").append(visible).append(", checkMainNameOnly=").append(checkMainNameOnly)
                    .append(", toStringRegex=").append(toStringRegex).append("]");
            return builder.toString();
        }

        private boolean testNames(IItem item) {
            if (item == null || this.objectName == null) {
                return false;
            }
            if (checkMainNameOnly) {
                if (this.searchNameUseRegex) {
                    return Pattern.matches(this.objectName, item.getName());
                } else {
                    return this.objectName.equalsIgnoreCase(item.getName());
                }
            } else {
                final String displayName = RenameCapability.displayName(item);
                if (this.searchNameUseRegex) {
                    return Pattern.matches(this.objectName, displayName);
                } else {
                    return this.objectName.equalsIgnoreCase(displayName);
                }
            }
        }

        @Override
        public boolean test(IItem item) {
            if (item == null) {
                return false;
            }
            if (visible != null && item.isVisible() != visible) {
                return false;
            }
            if (objectName != null && !this.testNames(item)) {
                return false;
            }
            if (this.capabilityChecks != null) {
                for (final Entry<ItemCapabilityNames, Boolean> entry : this.capabilityChecks.entrySet()) {
                    if (entry == null) {
                        continue;
                    }
                    final Boolean presenceCheck = entry.getValue();
                    if (presenceCheck == null) {
                        continue;
                    }
                    final ItemCapabilityNames name = entry.getKey();
                    if (name == null) {
                        continue;
                    }
                    ItemCapability capability = null;
                    switch (name) {
                    case CONTAINER:
                        capability = item.getItemContainerCapability();
                        break;
                    case EQUIPABLE:
                        capability = item.getEquipableCapability();
                        break;
                    case EVENTFUL:
                        capability = item.getGameEventProcessorCapability();
                        break;
                    case INTERACTABLE:
                        capability = item.getInteractableCapability();
                        break;
                    case LOCKING:
                        capability = item.getLockingCapability();
                        break;
                    case PEN:
                        capability = item.getCreatureContainerCapability();
                        break;
                    case RENAMEABLE:
                        capability = item.getRenameCapability();
                        break;
                    case USABLE:
                        capability = item.getUsableCapability();
                        break;
                    case WEAPON:
                        capability = item.getWeaponCapability();
                        break;
                    default:
                        break;
                    }
                    if (presenceCheck && capability == null) {
                        return false;
                    } else if (!presenceCheck && capability != null) {
                        return false;
                    }
                }
            }
            if (this.toStringRegex != null && !Pattern.matches(toStringRegex, item.toString())) {
                return false;
            }
            return true;
        }

    }

    public default Collection<IItem> filterItems(String objectName, boolean searchNameUseRegex,
            EnumMap<ItemCapabilityNames, Boolean> capabilities, Boolean visible, boolean checkMainNameOnly,
            String toStringRegex) {
        final ItemFilterQuery query = new ItemFilterQuery(objectName, searchNameUseRegex, capabilities, visible,
                checkMainNameOnly, toStringRegex);
        return this.filterItems(query);
    }

    public default Collection<IItem> filterItems(ItemFilterQuery query) {
        final Collection<IItem> retrieved = this.getItems();
        if (query == null || retrieved == null) {
            return retrieved;
        }
        Supplier<Collection<IItem>> sortSupplier = () -> new ArrayList<IItem>();
        return Collections.unmodifiableCollection(
                retrieved.stream().filter(query).collect(Collectors.toCollection(sortSupplier)));
    }

    public default Optional<IItem> getItemLike(ItemFilterQuery query) {
        return this.filterItems(query).stream().findFirst();
    }

    public default Optional<IItem> getItem(String name) {
        return this.filterItems(new ItemFilterQuery(name, true)).stream().findFirst();
    }

    public default boolean hasItem(String name, boolean useRegex) {
        return this.filterItems(new ItemFilterQuery(name, useRegex)).size() > 0;
    }

    public default boolean hasItem(String name) {
        return this.hasItem(name, false);
    }

    public default boolean hasItem(IItem item) {
        Collection<IItem> retrieved = this.getItems();
        return retrieved.contains(item);
    }

    public default boolean isEmpty() {
        return this.getItems().isEmpty();
    }

    public default int size() {
        return this.getItems().size();
    }

    @Override
    default String getTagName() {
        return "ItemContainer";
    }

}
