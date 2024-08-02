package com.lhf.game.item;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

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
            final String alternate = capability.getAlternateNames();
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
