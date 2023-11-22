package com.lhf.game.item.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.Builder;
import com.lhf.messages.out.SeeOutMessage.SeeCategory;

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
    public SeeOutMessage produceMessage() {
        SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
        for (Item thing : this.chestItems) {
            if (thing instanceof Takeable) {
                seeOutMessage.addSeen(SeeCategory.TAKEABLE, thing);
            } else {
                seeOutMessage.addSeen(SeeCategory.OTHER, thing);
            }
        }
        return seeOutMessage.Build();
    }

    @Override
    public SeeOutMessage produceMessage(Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeOutMessage.getBuilder();
        }
        seeOutMessage.setExaminable(this);
        for (Item thing : this.chestItems) {
            if (thing instanceof Takeable) {
                seeOutMessage.addSeen(SeeCategory.TAKEABLE, thing);
            } else {
                seeOutMessage.addSeen(SeeCategory.OTHER, thing);
            }
        }
        return seeOutMessage.Build();
    }

    public UUID getChestUuid() {
        return chestUuid;
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
            if (exact instanceof Takeable && exact.CheckNameRegex(name, 3)) {
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
    public Iterator<? extends Item> itemIterator() {
        return this.chestItems.iterator();
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
