package com.lhf.game.item;

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

    public static final class Locking implements LockingCapability {
        private final UUID lockUUID;
        private boolean locked = false;

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
        public void describe(ABuilder<?> seeEventBuilder) {
            if (seeEventBuilder == null) {
                return;
            }
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
