package com.lhf.game.item;

import java.io.Serializable;

import com.lhf.messages.events.SeeEvent.ABuilder;

public interface RenameCapability extends ItemCapability {
    public String getAlternateName();

    public RenameCapability rename(String newName);

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
            final String alternate = capability.getAlternateName();
            if (alternate != null && !alternate.isBlank()) {
                return alternate;
            }
        }
        return myItem.getName();
    }

    public static final class Renameable implements RenameCapability, Serializable {
        private String alternateName;

        @Override
        public String getAlternateName() {
            return this.alternateName;
        }

        @Override
        public RenameCapability rename(String newName) {
            if (newName != null && !newName.matches(IItem.ITEM_NAMES)) {
                throw new IllegalArgumentException(
                        String.format("Item name must match the regex '%s', but was '%s'", IItem.ITEM_NAMES, newName));
            }
            this.alternateName = newName;
            return this;
        }

        @Override
        public RenameCapability clear() {
            this.alternateName = null;
            return this;
        }

    }

}
