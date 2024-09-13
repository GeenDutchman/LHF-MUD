package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
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
        private String searchName;
        private ArrayList<String> searchNameRegexes = new ArrayList<>();
        private TreeMap<ItemCapability.ItemCapabilityNames, Boolean> capabilityChecks = new TreeMap<>();
        private Boolean visible = true;
        private boolean checkMainNameOnly = false;
        private ArrayList<String> toStringRegexes = new ArrayList<>();

        private ItemFilterQuery() {
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex) {
            if (searchNameUseRegex && objectName != null) {
                this.searchNameRegexes.add(objectName);
            } else {
                this.searchName = objectName;
            }
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex,
                EnumMap<ItemCapabilityNames, Boolean> capabilities) {
            this(objectName, searchNameUseRegex);
            this.capabilityChecks = capabilities != null ? new TreeMap<>(capabilities) : null;
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex,
                EnumMap<ItemCapabilityNames, Boolean> capabilities, Boolean visible, boolean checkMainNameOnly,
                String toStringRegex) {
            this(objectName, searchNameUseRegex, capabilities);
            this.visible = visible;
            this.checkMainNameOnly = checkMainNameOnly;
            if (toStringRegex != null) {
                this.toStringRegexes.add(toStringRegex);
            }
        }

        // Helper methods to merge attributes according to AND logic
        private <T> T combineAnd(T value1, T value2) {
            if (value1 == null)
                return value2;
            if (value2 == null)
                return value1;
            return value1.equals(value2) ? value1 : null; // Both must match, otherwise null
        }

        // Helper methods to merge attributes according to OR logic
        private <T> T combineOr(T value1, T value2) {
            return (value1 != null) ? value1 : value2; // At least one must be non-null
        }

        private ArrayList<String> combineRegexesAnd(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null) {
                list.addAll(firstRegex);
            }
            if (secondRegex != null) {
                list.addAll(secondRegex);
            }
            return list;
        }

        private ArrayList<String> combineRegexesOr(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null && secondRegex == null) {
                list.addAll(firstRegex);
            } else if (firstRegex == null && secondRegex != null) {
                list.addAll(secondRegex);
            } else if (firstRegex != null && secondRegex != null) {
                for (final String first : firstRegex) {
                    if (first == null) {
                        continue;
                    }
                    for (final String second : secondRegex) {
                        if (second != null) {
                            list.add(first + "|" + second);
                        }
                    }
                }
            }
            return list;
        }

        private <T> TreeMap<T, Boolean> combineMapsAnd(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
            TreeMap<T, Boolean> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<T, Boolean> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(), this.combineAnd(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private <T> TreeMap<T, Boolean> combineMapsOr(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
            TreeMap<T, Boolean> next = null;
            if (firstMap == null && secondMap == null) {
                return next;
            } else if (firstMap != null && secondMap == null) {
                next = new TreeMap<>(firstMap);
            } else if (firstMap == null && secondMap != null) {
                next = new TreeMap<>(secondMap);
            } else if (firstMap != null && secondMap != null) {
                next = new TreeMap<>(firstMap);
                for (Map.Entry<T, Boolean> entry : secondMap.entrySet()) {
                    next.put(entry.getKey(), this.combineOr(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        public ItemFilterQuery and(ItemFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }
            ItemFilterQuery next = new ItemFilterQuery();
            next.setSearchName(this.combineAnd(this.searchName, other.searchName));
            next.searchNameRegexes = this.combineRegexesAnd(this.searchNameRegexes, other.searchNameRegexes);
            next.capabilityChecks = this.combineMapsAnd(this.capabilityChecks, other.capabilityChecks);
            next.visible = this.combineAnd(this.visible, other.visible);
            next.checkMainNameOnly = this.combineAnd(this.checkMainNameOnly, other.checkMainNameOnly);
            next.toStringRegexes = this.combineRegexesAnd(this.toStringRegexes, other.toStringRegexes);
            return next;
        }

        public ItemFilterQuery or(ItemFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }
            ItemFilterQuery next = new ItemFilterQuery();
            next.setSearchName(this.combineOr(this.searchName, other.searchName));
            next.searchNameRegexes = this.combineRegexesOr(this.searchNameRegexes, other.searchNameRegexes);
            next.capabilityChecks = this.combineMapsOr(this.capabilityChecks, other.capabilityChecks);
            next.visible = this.combineOr(this.visible, other.visible);
            next.checkMainNameOnly = this.combineOr(this.checkMainNameOnly, other.checkMainNameOnly);
            next.toStringRegexes = this.combineRegexesOr(this.toStringRegexes, other.toStringRegexes);
            return next;
        }

        public ItemFilterQuery setSearchName(String searchName) {
            this.searchName = searchName;
            if (searchName != null && this.searchNameRegexes != null) {
                this.searchNameRegexes.clear();
            }
            return this;
        }

        public ItemFilterQuery addSearchNameRegex(String searchNameRegex) {
            if (this.searchNameRegexes == null) {
                this.searchNameRegexes = new ArrayList<>();
            }
            if (searchNameRegex != null) {
                this.searchNameRegexes.add(searchNameRegex);
                this.searchName = null;
            }
            return this;
        }

        public ItemFilterQuery setSearchNameRegexes(List<String> regexes) {
            if (this.searchNameRegexes == null) {
                this.searchNameRegexes = new ArrayList<>();
            }
            if (regexes == null || regexes.isEmpty()) {
                return this;
            }
            for (final String regex : regexes) {
                if (regex != null) {
                    this.searchNameRegexes.add(regex);
                }
            }
            return this;
        }

        public ItemFilterQuery clearSearchNameRegexes() {
            if (this.searchNameRegexes != null) {
                this.searchNameRegexes.clear();
            }
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

        public Map<ItemCapability.ItemCapabilityNames, Boolean> getCapabilityChecks() {
            if (this.capabilityChecks == null) {
                this.capabilityChecks = new TreeMap<>();
            }
            return capabilityChecks;
        }

        public ItemFilterQuery addToStringRegex(String regex) {
            if (this.toStringRegexes == null) {
                this.toStringRegexes = new ArrayList<>();
            }
            if (regex != null) {
                this.toStringRegexes.add(regex);
            }
            return this;
        }

        public ItemFilterQuery setToStringRegexes(List<String> regexes) {
            if (this.toStringRegexes == null) {
                this.toStringRegexes = new ArrayList<>();
            }
            if (regexes == null || regexes.isEmpty()) {
                return this;
            }
            for (final String regex : regexes) {
                if (regex != null) {
                    this.toStringRegexes.add(regex);
                }
            }
            return this;
        }

        public ItemFilterQuery clearToStringRegexes() {
            if (this.toStringRegexes != null) {
                this.toStringRegexes.clear();
            }
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ItemFilterQuery [searchName=").append(searchName).append(", searchNameRegexes=")
                    .append(searchNameRegexes).append(", capabilityChecks=").append(capabilityChecks)
                    .append(", visible=").append(visible).append(", checkMainNameOnly=").append(checkMainNameOnly)
                    .append(", toStringRegexes=").append(toStringRegexes).append("]");
            return builder.toString();
        }

        private boolean testNames(IItem item) {
            if (item == null || this.searchName == null) {
                return false;
            }
            if (checkMainNameOnly) {
                if (this.searchName != null) {
                    return this.searchName.equalsIgnoreCase(item.getName());
                } else if (this.searchNameRegexes != null) {
                    for (final String regex : this.searchNameRegexes) {
                        if (regex == null) {
                            continue;
                        }
                        if (!Pattern.matches(regex, item.getName())) {
                            return false;
                        }
                    }
                }
            } else {
                final String displayName = RenameCapability.displayName(item);
                if (this.searchName != null) {
                    return this.searchName.equalsIgnoreCase(displayName);
                } else if (this.searchNameRegexes != null) {
                    for (final String regex : this.searchNameRegexes) {
                        if (regex == null) {
                            continue;
                        }
                        if (!Pattern.matches(regex, displayName)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public boolean test(IItem item) {
            if (item == null) {
                return false;
            }
            if (visible != null && item.isVisible() != visible) {
                return false;
            }
            if (searchName != null && !this.testNames(item)) {
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
            if (this.toStringRegexes != null) {
                final String asString = item.toString();
                for (final String regex : this.toStringRegexes) {
                    if (regex == null) {
                        continue;
                    }
                    if (!Pattern.matches(regex, asString)) {
                        return false;
                    }
                }
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
