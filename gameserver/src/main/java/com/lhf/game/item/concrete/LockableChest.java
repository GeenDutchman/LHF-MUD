package com.lhf.game.item.concrete;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.ItemContainer;
import com.lhf.game.item.Item;
import com.lhf.game.item.concrete.LockKey.Lockable;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.SeeOutMessage.Builder;

public class LockableChest extends Chest implements Lockable {

    private class LockBypass implements ItemContainer {
        private LockBypass() {
        }

        @Override
        public String getName() {
            return LockableChest.this.getName();
        }

        @Override
        public String printDescription() {
            return LockableChest.this.printDescription();
        }

        @Override
        public Collection<Item> getItems() {
            return LockableChest.super.getItems();
        }

        @Override
        public boolean addItem(Item item) {
            return LockableChest.super.addItem(item);
        }

        @Override
        public Optional<Item> removeItem(String name) {
            return LockableChest.super.removeItem(name);
        }

        @Override
        public boolean removeItem(Item item) {
            return LockableChest.super.removeItem(item);
        }

        @Override
        public Iterator<? extends Item> itemIterator() {
            return LockableChest.super.itemIterator();
        }

    }

    protected final AtomicBoolean locked;

    private final static Note lockedNote = new Note("Chest Locked", false, "This chest is locked.");

    public LockableChest(ChestDescriptor descriptor, boolean isVisible) {
        super(descriptor, isVisible);
        this.locked = new AtomicBoolean(false);
    }

    public LockableChest(ChestDescriptor descriptor, boolean isVisible, boolean initialLock) {
        super(descriptor, isVisible);
        this.locked = new AtomicBoolean(initialLock);
    }

    @Override
    public boolean isEmpty() {
        return this.chestItems.isEmpty();
    }

    @Override
    public UUID getLockUUID() {
        return this.chestUuid;
    }

    @Override
    public boolean isUnlocked() {
        return this.locked.get();
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
    public String printDescription() {
        return super.printDescription() + (this.locked.get() ? " But it is locked." : " It is unlocked.");
    }

    @Override
    public SeeOutMessage produceMessage() {
        if (this.locked.get()) {
            SeeOutMessage.Builder seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this);
            return seeOutMessage.Build();
        }
        return super.produceMessage();
    }

    @Override
    public SeeOutMessage produceMessage(Builder seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeOutMessage.getBuilder();
        }
        seeOutMessage.setExaminable(this);
        if (this.locked.get()) {
            return seeOutMessage.Build();
        }
        return super.produceMessage(seeOutMessage);
    }

    @Override
    public Collection<Item> getItems() {
        if (this.locked.get()) {
            return List.of(LockableChest.lockedNote);
        }
        return super.getItems();
    }

    @Override
    public Optional<Item> removeItem(String name) {
        if (this.locked.get()) {
            return Optional.empty();
        }
        return super.removeItem(name);
    }

    @Override
    public boolean addItem(Item item) {
        if (this.locked.get()) {
            return false;
        }
        return super.addItem(item);
    }

    public ItemContainer bypass() {
        return new LockBypass();
    }

}
