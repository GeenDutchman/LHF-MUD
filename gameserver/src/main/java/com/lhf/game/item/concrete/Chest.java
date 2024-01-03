package com.lhf.game.item.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.LockableItemContainer;
import com.lhf.game.item.Item;
import com.lhf.game.item.Takeable;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.Builder;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public class Chest extends Item implements LockableItemContainer {
    protected final UUID chestUuid;
    protected final AtomicBoolean locked;
    protected final boolean removeOnEmpty;
    protected List<Item> chestItems;

    private final static Note lockedNote = new Note("Chest Locked", false, "This chest is locked.");

    public enum ChestDescriptor {
        RUSTY, SHINY, BLUE, SLIPPERY, WOODEN, COLORFUL, METAL, FANCY;

        public static String generateDescription(ChestDescriptor descriptor) {
            return descriptor != null ? descriptor.toString().toLowerCase() + " chest" : "nondescript chest";
        }
    }

    public Chest(ChestDescriptor descriptor, boolean isVisible) {
        super(ChestDescriptor.generateDescription(descriptor), isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
        this.descriptionString = "A " + this.descriptionString;
        this.locked = new AtomicBoolean(false);
        this.removeOnEmpty = false;
    }

    public Chest(ChestDescriptor descriptor, boolean isVisible, boolean initialLock, boolean removeOnEmpty) {
        super(ChestDescriptor.generateDescription(descriptor), isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
        this.descriptionString = "A " + this.descriptionString;
        this.locked = new AtomicBoolean(initialLock);
        this.removeOnEmpty = removeOnEmpty;
    }

    protected Chest(String name, boolean isVisible) {
        super(name, isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
        this.locked = new AtomicBoolean(false);
        this.removeOnEmpty = false;
    }

    protected Chest(String name, boolean isVisible, boolean initialLock, boolean removeOnEmpty) {
        super(name, isVisible);
        this.chestUuid = UUID.randomUUID();
        this.chestItems = new ArrayList<>();
        this.locked = new AtomicBoolean(initialLock);
        this.removeOnEmpty = removeOnEmpty;
    }

    @Override
    public Chest makeCopy() {
        return new Chest(this.getName(), this.checkVisibility(), this.locked.get(), this.removeOnEmpty);
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.printDescription() + ".");
        if (this.isUnlocked()) {
            sj.add("It is unlocked.");
            sj.add(this.isEmpty() ? "It is empty." : "Something is inside.");
        } else {
            sj.add("It is locked.");
            sj.add(this.isEmpty() ? "It seems to be empty." : "It doesn't seem to be empty.");
        }
        return sj.toString();
    }

    @Override
    public SeeEvent produceMessage() {
        SeeEvent.Builder seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        return this.produceMessage(seeOutMessage);
    }

    @Override
    public SeeEvent produceMessage(Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder();
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

    @Override
    public UUID getLockUUID() {
        return this.chestUuid;
    }

    @Override
    public boolean isUnlocked() {
        return !this.locked.get();
    }

    @Override
    public void unlock() {
        this.locked.set(false);
    }

    @Override
    public void lock() {
        this.locked.set(true);
    }

    @Override
    public boolean isEmpty() {
        return this.chestItems.isEmpty();
    }

    @Override
    public boolean addItem(Item item) {
        if (item == null || !this.isUnlocked()) {
            return false;
        }
        return this.chestItems.add(item);
    }

    @Override
    public Collection<Item> getItems() {
        if (!this.isUnlocked()) {
            return List.of(Chest.lockedNote);
        }
        return Collections.unmodifiableList(this.chestItems);
    }

    @Override
    public Optional<Item> removeItem(String name) {
        if (!this.isUnlocked()) {
            return Optional.empty();
        }
        Optional<Item> found = this.getItem(name);
        if (found.isPresent()) {
            this.chestItems.remove(found.get());
        }
        return found;
    }

    @Override
    public boolean removeItem(Item item) {
        if (!this.isUnlocked()) {
            return false;
        }
        return this.chestItems.remove(item);
    }

    @Override
    public Iterator<? extends Item> itemIterator() {
        return this.chestItems.iterator();
    }

    public boolean isRemoveOnEmpty() {
        return removeOnEmpty;
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

    private class ChestLockBypass implements LockableItemContainer.Bypass<Chest> {
        private ChestLockBypass() {
        }

        @Override
        public String getName() {
            return Chest.this.getName();
        }

        @Override
        public String printDescription() {
            return Chest.this.printDescription();
        }

        @Override
        public Collection<Item> getItems() {
            return Collections.unmodifiableList(Chest.this.chestItems);
        }

        @Override
        public boolean addItem(Item item) {
            return Chest.this.chestItems.add(item);
        }

        @Override
        public Optional<Item> removeItem(String name) {
            Optional<Item> found = this.getItem(name);
            if (found.isPresent()) {
                Chest.this.chestItems.remove(found.get());
            }
            return found;
        }

        @Override
        public boolean removeItem(Item item) {
            return Chest.this.chestItems.remove(item);
        }

        @Override
        public Iterator<? extends Item> itemIterator() {
            return Chest.this.chestItems.iterator();
        }

        @Override
        public Chest getOrigin() {
            return Chest.this;
        }

    }

    @Override
    public ChestLockBypass getBypass() {
        return new ChestLockBypass();
    }

}
