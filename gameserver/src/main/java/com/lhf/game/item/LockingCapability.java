package com.lhf.game.item;

import java.io.Serializable;
import java.util.UUID;

import com.lhf.game.Lockable;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface LockingCapability extends ItemCapability, Lockable {
    @Override
    default boolean isStateful() {
        return true;
    }

    public static LockingCapability generateLockingCapability() {
        return new Locking();
    }

    @Override
    public default void describe(ABuilder<?> seeEventBuilder) {
        if (seeEventBuilder == null) {
            return;
        }
        if (this.isUnlocked()) {
            seeEventBuilder.addExtraInfo("This item can be locked, with the right key. ");
        } else {
            seeEventBuilder.addExtraInfo("This item needs some type of key. ");
        }
    }

    public static final class Locking implements LockingCapability, Serializable {
        private final UUID lockUUID;
        private boolean locked = false;
        // TODO: have a good look at the key

        public Locking() {
            this.lockUUID = UUID.randomUUID();
            this.locked = false;
        }

        public Locking(UUID lockUUID, boolean locked) {
            this.lockUUID = lockUUID != null ? lockUUID : UUID.randomUUID();
            this.locked = locked;
        }

        @Override
        public ItemCapabilityNames getCapabilityName() {
            return ItemCapabilityNames.LOCKING;
        }

        @Override
        public UUID getLockUUID() {
            return this.lockUUID;
        }

        @Override
        public boolean isUnlocked() {
            return !this.locked;
        }

        @Override
        public void unlock() {
            this.locked = false;
        }

        @Override
        public void lock() {
            this.locked = true;
        }
    }
}
