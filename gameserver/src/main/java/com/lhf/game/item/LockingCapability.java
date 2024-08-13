package com.lhf.game.item;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.Lockable;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface LockingCapability extends ItemCapability, Lockable {
    @Override
    default boolean isStateful() {
        return true;
    }

    public static LockingCapability generateLockingCapability() {
        return new Locking(null);
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

    public static boolean toggleLock(CommandContext ctx, IItem myItem) {
        if (ctx == null) {
            return false;
        }
        final ICreature creature = ctx.getCreature();
        if (creature == null) {
            return false;
        }
        if (myItem == null) {
            return false; // TODO: error message
        }
        final LockingCapability capability = myItem.getLockingCapability();
        if (capability == null) {
            return false;
        }
        boolean unlocked = capability.isUnlocked();
        if (capability.isAuthorized(creature)) {
            if (unlocked) {
                capability.lock();
            } else {
                capability.unlock();
            }
            return true;
        }

        return false;
    }

    public static enum Delta implements ICapabilityDelta, Consumer<LockingCapability> {
        NOOP {
            @Override
            public void accept(LockingCapability arg0) {
                // does nothing
            }
        },
        TOGGLE {
            @Override
            public void accept(LockingCapability arg0) {
                if (arg0 != null) {
                    if (arg0.isUnlocked()) {
                        arg0.lock();
                    } else {
                        arg0.unlock();
                    }
                }
            }
        },
        LOCK {
            @Override
            public void accept(LockingCapability arg0) {
                if (arg0 != null) {
                    arg0.lock();
                }
            }
        },
        UNLOCK {
            @Override
            public void accept(LockingCapability arg0) {
                if (arg0 != null) {
                    arg0.unlock();
                }
            }
        };

        @Override
        public abstract void accept(LockingCapability arg0);

        public Delta invert() {
            switch (this) {
            case LOCK:
                return UNLOCK;
            case TOGGLE:
                return TOGGLE;
            case UNLOCK:
                return LOCK;
            case NOOP:
            default:
                return NOOP;
            }
        }

        @Override
        public void buildOutput(RichOutputBuilder builder) {
            if (builder == null) {
                return;
            }
            builder.appendString("After application, the locking capability is");
            switch (this) {
            case LOCK:
                builder.appendString("locked.");
                break;
            case UNLOCK:
                builder.appendString("unlocked.");
                break;
            case NOOP:
                builder.appendString("unchanged.");
                break;
            case TOGGLE:
                builder.appendString("toggled.");
                break;
            default:
                builder.appendString("unchanged.");
                break;

            }
        }
    }

    public default void acceptDelta(Delta delta) {
        if (delta != null) {
            delta.accept(this);
        }
    }

    public static final class Locking implements LockingCapability, Serializable {
        private final UUID lockUUID;
        private boolean locked = false;
        // TODO: have a good look at the key

        public static Builder getBuilder() {
            return new Builder();
        }

        public static final class Builder {
            private UUID lockUUID;
            private boolean locked;

            public Builder reset() {
                this.lockUUID = null;
                this.locked = true;
                return this;
            }

            public UUID getLockUUID() {
                return lockUUID != null ? lockUUID : UUID.randomUUID();
            }

            public Builder setLockUUID(UUID lockUUID) {
                this.lockUUID = lockUUID;
                return this;
            }

            public boolean isLocked() {
                return locked;
            }

            public Builder setLocked(boolean locked) {
                this.locked = locked;
                return this;
            }

            public Locking build() {
                return new Locking(this);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Builder [lockUUID=").append(lockUUID).append(", locked=").append(locked).append("]");
                return builder.toString();
            }

        }

        private Locking(Builder builder) {
            if (builder == null) {
                this.lockUUID = UUID.randomUUID();
                this.locked = false;
            } else {
                this.lockUUID = builder.getLockUUID();
                this.locked = builder.isLocked();
            }
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

        @Override
        public int hashCode() {
            return Objects.hash(lockUUID);
        }

        @Override
        public String toString() {
            StringBuilder builder2 = new StringBuilder();
            builder2.append("Locking [lockUUID=").append(lockUUID).append(", locked=").append(locked).append("]");
            return builder2.toString();
        }

    }
}
