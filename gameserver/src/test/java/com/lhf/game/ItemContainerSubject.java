package com.lhf.game;

import java.util.Objects;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Truth;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.item.IItem;

public class ItemContainerSubject extends IterableSubject {
    public static Factory<ItemContainerSubject, ItemContainer> itemContainers() {
        return ItemContainerSubject::new;
    }

    public static ItemContainerSubject assertThat(ItemContainer actual) {
        return Truth.assertAbout(itemContainers()).that(actual);
    }

    private final ItemContainer actual;

    protected ItemContainerSubject(FailureMetadata metadata, ItemContainer actual) {
        super(metadata, actual != null ? actual.getItems() : null);
        this.actual = actual;
    }

    public StringSubject name() {
        return check("getName()").that(actual.getName());
    }

    public IterableSubject items() {
        return check("getItems()").that(actual.getItems());
    }

    public void itemIsAdded(IItem item) {
        check("addItem(%s)", item).that(actual.addItem(item)).isTrue();
        this.items().contains(item);
    }

    public void itemIsNotAdded(IItem item) {
        check("addItem(%s)", item).that(actual.addItem(item)).isFalse();
        this.items().doesNotContain(item);
    }

    public void itemIsRemoved(IItem item) {
        check("removeItem(%s)", item).that(actual.removeItem(item)).isTrue();
        this.items().doesNotContain(item);
    }

    public IterableSubject filteredItems(ItemFilterQuery query) {
        return check("filterItems(%s)", query).that(actual.filterItems(query));
    }

    public void hasItem(IItem item) {
        this.items().contains(item);
    }

    public void doesNotHaveItem(IItem item) {
        this.items().doesNotContain(item);
    }

    @Override
    public void isEqualTo(Object expected) {
        @SuppressWarnings("UndefinedEquals") // method contract requires testing iterables for equality
        boolean equal = Objects.equals(actual, expected);
        if (equal) {
            return;
        }

        if (expected instanceof ItemContainer expectedIC) {
            containsExactlyElementsIn(expectedIC.getItems());
        } else {
            super.isEqualTo(expected);
        }
    }
}
