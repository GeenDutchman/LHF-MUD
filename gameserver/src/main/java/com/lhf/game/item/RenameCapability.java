package com.lhf.game.item;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

import com.lhf.messages.events.SeeEvent.ABuilder;

public interface RenameCapability extends ItemCapability {
    public String getAlternateName();

    public Deque<String> getNameStack();

    public String popName();

    public RenameCapability addName(String newName);

    public RenameCapability clear();

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        return;
    }

    public static class Delta implements Consumer<RenameCapability> {
        private final String nameToAdd;
        private final boolean popName;
        private final boolean clearNames;

        public static Delta ofClearNames() {
            return new Delta(null, false, true);
        }

        public static Delta ofPopName() {
            return new Delta(null, true, false);
        }

        public static Delta ofNextName(String name) {
            if (name == null || !name.matches(IItem.ITEM_NAMES)) {
                return null;
            }
            return new Delta(name, false, false);
        }

        private Delta(String nameToAdd, boolean popName, boolean clearNames) {
            this.nameToAdd = nameToAdd;
            this.popName = popName;
            this.clearNames = clearNames;
        }

        @Override
        public void accept(RenameCapability arg0) {
            if (arg0 != null) {
                if (this.popName) {
                    arg0.popName();
                }
                if (this.clearNames) {
                    arg0.clear();
                }
                if (nameToAdd != null) {
                    arg0.addName(nameToAdd);
                }
            }
        }

    }

    public default void acceptDelta(Delta delta) {
        if (delta != null) {
            delta.accept(this);
        }
    }

    @Override
    public default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.RENAMEABLE;
    }

    public static RenameCapability generateRenameCapability() {
        return new Renameable();
    }

    public static String displayName(IItem myItem) {
        if (myItem == null) {
            return null;
        }
        final RenameCapability capability = myItem.getRenameCapability();
        if (capability != null) {
            final String alternate = capability.getAlternateName();
            if (alternate != null && !alternate.isBlank()) {
                return alternate;
            }
        }
        return myItem.getName();
    }

    public static final class Renameable implements RenameCapability, Serializable {
        private LinkedList<String> alternateNames;

        @Override
        public String getAlternateName() {
            return this.alternateNames != null ? this.alternateNames.peekFirst() : null;
        }

        @Override
        public Deque<String> getNameStack() {
            return this.alternateNames;
        }

        @Override
        public RenameCapability addName(String newName) {
            if (newName == null) {
                return this;
            }
            if (!newName.matches(IItem.ITEM_NAMES)) {
                throw new IllegalArgumentException(
                        String.format("Item name must match the regex '%s', but was '%s'", IItem.ITEM_NAMES, newName));
            }
            if (this.alternateNames == null) {
                this.alternateNames = new LinkedList<>();
            }
            this.alternateNames.addFirst(newName);
            return this;
        }

        @Override
        public RenameCapability clear() {
            if (this.alternateNames != null) {
                this.alternateNames.clear();
            }
            return this;
        }

        @Override
        public String popName() {
            return this.alternateNames != null ? this.alternateNames.removeFirst() : null;
        }

    }

}
