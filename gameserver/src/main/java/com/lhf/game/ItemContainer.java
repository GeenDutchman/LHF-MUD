package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.AItem;
import com.lhf.game.item.ItemVisitor;

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

    public enum ItemFilters {
        CLASS_NAME, OBJECT_NAME, TYPE, VISIBILITY;
    }

    public static class ItemFilterQuery implements Predicate<IItem> {
        public EnumSet<ItemFilters> filters = EnumSet.noneOf(ItemFilters.class);
        public String objectName;
        public Integer objectNameRegexLen;
        public String className;
        public transient Class<? extends IItem> clazz;
        public Boolean visible;

        @Override
        public int hashCode() {
            return Objects.hash(filters, objectName, objectNameRegexLen, className, clazz, visible);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ItemFilterQuery))
                return false;
            ItemFilterQuery other = (ItemFilterQuery) obj;
            return Objects.equals(filters, other.filters) && Objects.equals(objectName, other.objectName)
                    && Objects.equals(objectNameRegexLen, other.objectNameRegexLen)
                    && Objects.equals(className, other.className) && Objects.equals(clazz, other.clazz)
                    && Objects.equals(visible, other.visible);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ItemFilterQuery [filters=").append(filters).append(", objectName=").append(objectName)
                    .append(", objectNameRegexLen=").append(objectNameRegexLen).append(", className=").append(className)
                    .append(", clazz=").append(clazz).append(", visible=").append(visible).append("]");
            return builder.toString();
        }

        @Override
        public boolean test(IItem item) {
            if (item == null) {
                return false;
            }
            if (filters == null || filters.isEmpty()) {
                return true;
            }
            if (filters.contains(ItemFilters.VISIBILITY) && visible != null && item.isVisible() != visible) {
                return false;
            }
            if (filters.contains(ItemFilters.CLASS_NAME) && className != null && !className.equals(item.getName())) {
                return false;
            }
            if (filters.contains(ItemFilters.OBJECT_NAME) && objectName != null
                    && !(objectNameRegexLen != null ? item.CheckNameRegex(objectName, objectNameRegexLen)
                            : item.checkName(objectName))) {
                return false;
            }
            if (filters.contains(ItemFilters.TYPE) && clazz != null && !clazz.isInstance(item)) {
                return false;
            }
            return true;
        }

    }

    public default Collection<IItem> filterItems(EnumSet<ItemFilters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends AItem> clazz, Boolean isVisible) {
        ItemFilterQuery query = new ItemFilterQuery();
        query.filters = filters;
        query.className = className;
        query.objectName = objectName;
        query.objectNameRegexLen = objNameRegexLen;
        query.clazz = clazz;
        query.visible = isVisible;
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

    public default Optional<IItem> getItem(String name) {
        return this.filterItems(EnumSet.of(ItemFilters.OBJECT_NAME), null, name, 3, null, null).stream().findAny();
    }

    public default boolean hasItem(String name, Integer minimumLength) {
        return this.filterItems(EnumSet.of(ItemFilters.OBJECT_NAME), null, name, minimumLength, null, null).size() > 0;
    }

    public default boolean hasItem(String name) {
        return this.hasItem(name, 3);
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

}
