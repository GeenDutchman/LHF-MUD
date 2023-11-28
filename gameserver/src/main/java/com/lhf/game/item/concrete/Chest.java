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
    // TODO: #131 implement lockable Chests and chests locked by monsters
    protected final UUID chestUuid;
    protected List<Item> chestItems;

    public enum ChestDescriptor {
        RUSTY, SHINY, BLUE, SLIPPERY, WOODEN, COLORFUL, METAL, FANCY;
    }

    public Chest(ChestDescriptor descriptor, boolean isVisible) {
        super(descriptor != null ? descriptor.toString().toLowerCase() + " chest" : "nondescript chest", isVisible);
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
        return this.produceMessage(seeOutMessage);
    }

    @Override
    public SeeOutMessage produceMessage(Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeOutMessage.getBuilder();
        }
        seeOutMessage.setExaminable(this);
        for (Item thing : this.getItems()) {
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
        Optional<Item> found = this.getItem(name);
        if (found.isPresent()) {
            this.chestItems.remove(found.get());
        }
        return found;
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
