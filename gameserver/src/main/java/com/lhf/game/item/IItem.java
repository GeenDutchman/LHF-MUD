package com.lhf.game.item;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import com.lhf.Examinable;

public interface IItem extends Examinable {

    public final static class ItemID implements Comparable<ItemID> {
        private final UUID id;

        public ItemID() {
            this.id = UUID.randomUUID();
        }

        public UUID getId() {
            return id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ItemID))
                return false;
            ItemID other = (ItemID) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            return this.id.toString();
        }

        @Override
        public int compareTo(ItemID arg0) {
            return this.id.compareTo(arg0.id);
        }

    }

    ItemID getItemID();

    IItem makeCopy();

    void acceptItemVisitor(ItemVisitor visitor);

    String getClassName();

    boolean isVisible();

    boolean isTakeable();

    @Override
    String getName();

    public default Logger getLogger() {
        final String itemID = this.getItemID().toString();
        final String name = RenameCapability.displayName(this);
        return Logger.getLogger(String.format("%s.%s.%s", this.getClass().getName(), itemID, name));
    }

    default boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name);
    }

    default boolean CheckNameRegex(String possName, Integer minimumLength) {
        Integer min = minimumLength;
        if (min < 0) {
            min = 0;
        }
        if (this.getName().length() < min) {
            min = this.getName().length();
        }
        if (min > this.getName().length()) {
            min = this.getName().length();
        }
        if (possName.length() < min || possName.length() > this.getName().length()) {
            return false;
        }
        if (this.checkName(possName)) {
            return true;
        }
        if (possName.matches("[^ a-zA-Z_-]") || possName.contains("*")) {
            return false;
        }
        try {
            return this.getName().matches("(?i).*" + possName + ".*");
        } catch (PatternSyntaxException pse) {
            pse.printStackTrace();
            return false;
        }
    }

    @Override
    public default String getTagName() {
        return "item";
    }

    public interface IItemBuilder {
        public String getName();

        public String getDescriptionString();

        public String getTagName();

        public boolean isTakeable();

        public boolean isVisible();

        public Collection<ItemCapability> getCapabilities();

        public RenameCapability getRenameCapability();

        public UsableCapability getUsableCapability();

        public LockingCapability getLockingCapability();

        public EquipableCapability getEquipableCapability();

        public WeaponCapability getWeaponCapability();

        public InteractableCapability getInteractableCapability();

        public IItem build();
    }

    public RenameCapability getRenameCapability();

    public UsableCapability getUsableCapability();

    public LockingCapability getLockingCapability();

    public EquipableCapability getEquipableCapability();

    public WeaponCapability getWeaponCapability();

    public InteractableCapability getInteractableCapability();

}