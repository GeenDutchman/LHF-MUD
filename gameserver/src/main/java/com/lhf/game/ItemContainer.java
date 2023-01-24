package com.lhf.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.lhf.Examinable;
import com.lhf.game.item.Item;

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

    public enum Filters {
        CLASS_NAME, OBJECT_NAME, VISIBILITY;
    }

    public default Collection<Item> filter(EnumSet<Filters> filters, String className, String objectName,
            Integer objNameRegexLen, Boolean isVisible) {
        Collection<Item> retrieved = this.getItems();
        Supplier<Collection<Item>> sortSupplier = () -> new ArrayList<Item>();
        return retrieved.stream()
                .filter(item -> item != null)
                .filter(item -> !filters.contains(Filters.VISIBILITY)
                        || (isVisible != null && item.checkVisibility() == isVisible))
                .filter(item -> !filters.contains(Filters.CLASS_NAME)
                        || (className != null && className.equals(item.getClassName())))
                .filter(item -> !filters.contains(Filters.OBJECT_NAME) || (objectName != null
                        && (objNameRegexLen != null ? item.CheckNameRegex(objectName, objNameRegexLen)
                                : item.checkName(objectName))))
                .collect(Collectors.toCollection(sortSupplier));
    }

    public default Optional<Item> getItem(String name) {
        Collection<Item> retrieved = this.getItems();
        return retrieved.stream().filter(item -> item != null && item.CheckNameRegex(name, 3)).findAny();
    }

    public default boolean hasItem(String name, Integer minimumLength) {
        Collection<Item> retrieved = this.getItems();
        return retrieved.stream().anyMatch(item -> item != null && item.CheckNameRegex(name, minimumLength));
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
