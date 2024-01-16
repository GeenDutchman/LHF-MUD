package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
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

    public default Collection<IItem> filterItems(EnumSet<ItemFilters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends Item> clazz, Boolean isVisible) {
        Collection<IItem> retrieved = this.getItems();
        Supplier<Collection<IItem>> sortSupplier = () -> new ArrayList<IItem>();
        return Collections.unmodifiableCollection(retrieved.stream()
                .filter(item -> item != null)
                .filter(item -> !filters.contains(ItemFilters.VISIBILITY)
                        || (isVisible != null && item.isVisible() == isVisible))
                .filter(item -> !filters.contains(ItemFilters.CLASS_NAME)
                        || (className != null && className.equals(item.getClassName())))
                .filter(item -> !filters.contains(ItemFilters.OBJECT_NAME) || (objectName != null
                        && (objNameRegexLen != null ? item.CheckNameRegex(objectName, objNameRegexLen)
                                : item.checkName(objectName))))
                .filter(item -> !filters.contains(ItemFilters.TYPE) || (clazz != null && clazz.isInstance(item)))
                .collect(Collectors.toCollection(sortSupplier)));
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
