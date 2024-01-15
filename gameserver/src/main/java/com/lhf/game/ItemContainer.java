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
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemVisitor;

public interface ItemContainer extends Examinable {

    /**
     * This returns an immutable Collection of Items
     * 
     * @return Immutable collection
     */
    public abstract Collection<Item> getItems();

    public abstract boolean addItem(Item item);

    public abstract Optional<Item> removeItem(String name);

    public abstract boolean removeItem(Item item);

    public abstract Iterator<? extends Item> itemIterator();

    public static boolean transfer(ItemContainer from, ItemContainer to, Predicate<Item> predicate, boolean copyItem) {
        if (from == null || to == null) {
            return false;
        }
        boolean changed = false;
        for (Iterator<? extends Item> it = from.itemIterator(); it.hasNext();) {
            Item item = it.next();
            if (item != null && (predicate != null ? predicate.test(item) : true)
                    && (copyItem ? to.addItem(item.makeCopy()) : to.addItem(item))) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }

    public default void acceptItemVisitor(ItemVisitor visitor) {
        for (Item item : this.getItems()) {
            item.acceptItemVisitor(visitor);
        }
    }

    public enum ItemFilters {
        CLASS_NAME, OBJECT_NAME, TYPE, VISIBILITY;
    }

    public default Collection<Item> filterItems(EnumSet<ItemFilters> filters, String className, String objectName,
            Integer objNameRegexLen, Class<? extends Item> clazz, Boolean isVisible) {
        Collection<Item> retrieved = this.getItems();
        Supplier<Collection<Item>> sortSupplier = () -> new ArrayList<Item>();
        return Collections.unmodifiableCollection(retrieved.stream()
                .filter(item -> item != null)
                .filter(item -> !filters.contains(ItemFilters.VISIBILITY)
                        || (isVisible != null && item.checkVisibility() == isVisible))
                .filter(item -> !filters.contains(ItemFilters.CLASS_NAME)
                        || (className != null && className.equals(item.getClassName())))
                .filter(item -> !filters.contains(ItemFilters.OBJECT_NAME) || (objectName != null
                        && (objNameRegexLen != null ? item.CheckNameRegex(objectName, objNameRegexLen)
                                : item.checkName(objectName))))
                .filter(item -> !filters.contains(ItemFilters.TYPE) || (clazz != null && clazz.isInstance(item)))
                .collect(Collectors.toCollection(sortSupplier)));
    }

    public default Optional<Item> getItem(String name) {
        return this.filterItems(EnumSet.of(ItemFilters.OBJECT_NAME), null, name, 3, null, null).stream().findAny();
    }

    public default boolean hasItem(String name, Integer minimumLength) {
        return this.filterItems(EnumSet.of(ItemFilters.OBJECT_NAME), null, name, minimumLength, null, null).size() > 0;
    }

    public default boolean hasItem(String name) {
        return this.hasItem(name, 3);
    }

    public default boolean hasItem(Item item) {
        Collection<Item> retrieved = this.getItems();
        return retrieved.contains(item);
    }

    public default boolean isEmpty() {
        return this.getItems().isEmpty();
    }

    public default int size() {
        return this.getItems().size();
    }

}
