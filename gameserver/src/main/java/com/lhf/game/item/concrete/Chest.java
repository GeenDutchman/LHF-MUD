package com.lhf.game.item.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.Item;

public class Chest extends Item implements ItemContainer {
    protected final UUID chestUuid;
    protected List<Item> chestItems;

    public enum ChestDescriptor {
        RUSTY, SHINY, ORANGE, OILY, WOODEN, COLORFUL, METAL, ORNATE;
    }

    public Chest(ChestDescriptor descriptor, boolean isVisible) {
        super(descriptor != null ? descriptor.toString().toLowerCase() + " chest" : "unobtrusive chest", isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
        this.descriptionString = "A " + this.descriptionString;
    }

    protected Chest(String name, boolean isVisible) {
        super(name, isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
    }

    @Override
    public String printDescription() {
        return super.printDescription() + (this.isEmpty() ? ". It is empty." : ". It doesn't seem to be empty.");
    }

    @Override
    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        return this.chestItems.add(item);
    }

    @Override
    public Collection<Item> getItems() {
        return Collections.unmodifiableList(this.chestItems);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        for (Item exact : this.chestItems) {
            if (exact.CheckNameRegex(name, 3)) {
                this.chestItems.remove(exact);
                return Optional.of(exact);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(Item item) {
        return this.chestItems.remove(item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chestUuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Chest))
            return false;
        Chest other = (Chest) obj;
        return Objects.equals(chestUuid, other.chestUuid);
    }

}
