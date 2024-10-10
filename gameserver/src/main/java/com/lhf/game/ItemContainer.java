package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
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
        private final String searchName;
        private final List<String> searchNameRegexes;
        private final NavigableMap<ItemCapability.ItemCapabilityNames, Boolean> capabilityChecks;
        private final Boolean visible;
        private final boolean checkMainNameOnly;
        private final List<String> toStringRegexes;

        public ItemFilterQuery() {
            searchName = null;
            searchNameRegexes = null;
            capabilityChecks = null;
            visible = true;
            checkMainNameOnly = false;
            toStringRegexes = null;
        }

        public ItemFilterQuery(ItemFilterQuery other) {
            if (other == null) {
                searchName = null;
                searchNameRegexes = null;
                capabilityChecks = null;
                visible = true;
                checkMainNameOnly = false;
                toStringRegexes = null;
                return;
            }
            this.searchName = other.searchName;
            if (other.searchNameRegexes != null) {
                this.searchNameRegexes = Collections.unmodifiableList(List.copyOf(other.searchNameRegexes));
            } else {
                this.searchNameRegexes = null;
            }
            if (other.capabilityChecks != null) {
                this.capabilityChecks = Collections.unmodifiableNavigableMap(new TreeMap<>(other.capabilityChecks));
            } else {
                this.capabilityChecks = null;
            }
            this.visible = other.visible;
            this.checkMainNameOnly = other.checkMainNameOnly;
            if (other.toStringRegexes != null) {
                this.toStringRegexes = Collections.unmodifiableList(List.copyOf(other.toStringRegexes));
            } else {
                this.toStringRegexes = null;
            }
        }

        public ItemFilterQuery(String objectName, boolean searchNameUseRegex, boolean mainNameOnly) {
            capabilityChecks = null;
            visible = true;
            checkMainNameOnly = mainNameOnly;
            toStringRegexes = null;
            if (searchNameUseRegex && objectName != null) {
                this.searchNameRegexes = List.of(objectName);
                this.searchName = null;
            } else {
                this.searchName = objectName;
                this.searchNameRegexes = null;
            }
        }

        public ItemFilterQuery(String searchName, List<String> searchNameRegexes,
                Map<ItemCapabilityNames, Boolean> capabilityChecks, Boolean visible, boolean checkMainNameOnly,
                List<String> toStringRegexes) {
            this.searchName = searchName;
            this.searchNameRegexes = searchNameRegexes != null
                    ? Collections.unmodifiableList(List.copyOf(searchNameRegexes))
                    : null;
            this.capabilityChecks = capabilityChecks != null
                    ? Collections.unmodifiableNavigableMap(new TreeMap<>(capabilityChecks))
                    : null;
            this.visible = visible;
            this.checkMainNameOnly = checkMainNameOnly;
            this.toStringRegexes = toStringRegexes != null ? Collections.unmodifiableList(List.copyOf(toStringRegexes))
                    : null;
        }

        public static ItemFilterQuery withName(String objectName, boolean useRegex, boolean mainNameOnly) {
            return new ItemFilterQuery(objectName, useRegex, mainNameOnly);
        }

        public static ItemFilterQuery withCapability(ItemCapabilityNames capability) {
            return new ItemFilterQuery(null, null, capability != null ? Map.of(capability, true) : null, null, false,
                    null);
        }

        public static ItemFilterQuery withCapabilities(Map<ItemCapabilityNames, Boolean> caps) {
            return new ItemFilterQuery(null, null, caps, null, false, null);
        }

        public static ItemFilterQuery withCapabilities(Collection<ItemCapabilityNames> capabilities) {
            return new ItemFilterQuery(null, null, capabilities != null ? capabilities.stream()
                    .filter(cap -> cap != null).collect(Collectors.toMap(cap -> cap, cap -> true)) : null, null, false,
                    null);
        }

        public static ItemFilterQuery withoutCapability(ItemCapabilityNames capability) {
            return new ItemFilterQuery(null, null, capability != null ? Map.of(capability, false) : null, null, false,
                    null);
        }

        public static ItemFilterQuery withoutCapabilities(Collection<ItemCapabilityNames> capabilities) {
            return new ItemFilterQuery(null, null, capabilities != null ? capabilities.stream()
                    .filter(cap -> cap != null).collect(Collectors.toMap(cap -> cap, cap -> false)) : null, null, false,
                    null);
        }

        public static ItemFilterQuery withInvisibility() {
            return new ItemFilterQuery(null, null, null, false, false, null);
        }

        public static ItemFilterQuery withToStringRegex(String regex) {
            return new ItemFilterQuery(null, null, null, null, false, regex != null ? List.of(regex) : null);
        }

        // Helper methods to merge attributes according to AND logic
        private static <T> T combineAnd(T value1, T value2) {
            if (value1 == null)
                return value2;
            if (value2 == null)
                return value1;
            return value1.equals(value2) ? value1 : null; // Both must match, otherwise null
        }

        // Helper methods to merge attributes according to OR logic
        private static <T> T combineOr(T value1, T value2) {
            return (value1 != null) ? value1 : value2; // At least one must be non-null
        }

        private static List<String> combineRegexesAnd(List<String> firstRegex, List<String> secondRegex) {
            ArrayList<String> list = new ArrayList<>();
            if (firstRegex != null) {
                list.addAll(firstRegex);
            }
            if (secondRegex != null) {
                list.addAll(secondRegex);
            }
            return list;
        }

        private static List<String> combineRegexesOr(List<String> firstRegex, List<String> secondRegex) {
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

        private static <T> NavigableMap<T, Boolean> combineMapsAnd(Map<T, Boolean> firstMap,
                Map<T, Boolean> secondMap) {
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
                    next.put(entry.getKey(),
                            ItemFilterQuery.combineAnd(entry.getValue(), firstMap.get(entry.getKey())));
                }
            }
            return next;
        }

        private static <T> NavigableMap<T, Boolean> combineMapsOr(Map<T, Boolean> firstMap, Map<T, Boolean> secondMap) {
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
                    next.put(entry.getKey(), ItemFilterQuery.combineOr(entry.getValue(), firstMap.get(entry.getKey())));
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

            return new ItemFilterQuery(ItemFilterQuery.combineAnd(this.searchName, other.searchName),
                    ItemFilterQuery.combineRegexesAnd(this.searchNameRegexes, other.searchNameRegexes),
                    ItemFilterQuery.combineMapsAnd(this.capabilityChecks, other.capabilityChecks),
                    ItemFilterQuery.combineAnd(this.visible, other.visible),
                    ItemFilterQuery.combineAnd(this.checkMainNameOnly, other.checkMainNameOnly),
                    ItemFilterQuery.combineRegexesAnd(this.toStringRegexes, other.toStringRegexes));

        }

        public ItemFilterQuery or(ItemFilterQuery other) {
            if (other == null) {
                return this;
            } else if (other == this) {
                return this;
            }
            return new ItemFilterQuery(ItemFilterQuery.combineOr(this.searchName, other.searchName),
                    ItemFilterQuery.combineRegexesOr(this.searchNameRegexes, other.searchNameRegexes),
                    ItemFilterQuery.combineMapsOr(this.capabilityChecks, other.capabilityChecks),
                    ItemFilterQuery.combineOr(this.visible, other.visible),
                    ItemFilterQuery.combineOr(this.checkMainNameOnly, other.checkMainNameOnly),
                    ItemFilterQuery.combineRegexesOr(this.toStringRegexes, other.toStringRegexes));
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
        ItemFilterQuery query = new ItemFilterQuery(objectName, searchNameUseRegex, checkMainNameOnly);
        if (capabilities != null) {
            query = query.and(ItemFilterQuery.withCapabilities(capabilities));
        }
        if (!visible) {
            query = query.and(ItemFilterQuery.withInvisibility());
        }
        if (toStringRegex != null) {
            query = query.and(ItemFilterQuery.withToStringRegex(toStringRegex));
        }
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
        return this.filterItems(new ItemFilterQuery(name, true, false)).stream().findFirst();
    }

    public default boolean hasItem(String name, boolean useRegex) {
        return this.filterItems(new ItemFilterQuery(name, useRegex, false)).size() > 0;
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
