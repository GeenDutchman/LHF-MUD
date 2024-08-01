package com.lhf.game.item;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import com.lhf.Examinable;

public interface IItem extends Examinable {

    public static final String ITEM_NAMES = "^(?:\\w{1,2} )*\\w{3,}(?: \\w+)*$";

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

    /**
     * Describes if the item is visible
     */
    boolean isVisible();

    /**
     * Describes if the item is takeable, or if it stays put
     */
    boolean isTakeable();

    /**
     * Gets the name by which the item is known
     */
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

    /**
     * Gets the tag name for the item
     */
    @Override
    public default String getTagName() {
        return "item";
    }

    /**
     * This interface defines what items are needed to build an IItem
     */
    public interface IItemBuilder {
        /**
         * Resets the builder
         * 
         * @return fluent builder
         */
        public IItemBuilder reset();

        /**
         * Gets the name by which the item is known
         */
        public String getName();

        /**
         * Gets the plain description of the item
         */
        public String getDescriptionString();

        /**
         * Gets the tag name for the item
         */
        public String getTagName();

        /**
         * Describes if the item is takeable, or if it stays put
         */
        public boolean isTakeable();

        /**
         * Describes if the item is visible
         */
        public boolean isVisible();

        // /**
        // * Gets the Collection of capabilities that the item has
        // */
        // public Collection<ItemCapability> getCapabilities();

        /**
         * Returns the {@link com.lhf.game.item.RenameCapability Renaming Capability }
         * of the item, if it has one
         * 
         * @return RenameCapability or null
         */
        public RenameCapability getRenameCapability();

        /**
         * Returns the {@link com.lhf.game.item.UsableCapability Usable Capability } of
         * the item, if it has one
         * 
         * @return UsableCapability or null
         */
        public UsableCapability getUsableCapability();

        /**
         * Returns the {@link com.lhf.game.item.LockingCapability Locking Capability }
         * of the item, if it has one
         * 
         * @return LockingCapability or null
         */
        public LockingCapability getLockingCapability();

        /**
         * Returns the {@link com.lhf.game.item.UsableCapability Usable Capability } of
         * the item, if it has one
         * 
         * @return UsableCapability or null
         */
        public EquipableCapability getEquipableCapability();

        /**
         * Returns the {@link com.lhf.game.item.WeaponCapability Equipable Capability }
         * of the item, if it has one
         * 
         * @return WeaponCapability or null
         */
        public WeaponCapability getWeaponCapability();

        /**
         * Returns the {@link com.lhf.game.item.InteractableCapability Interactable
         * Capability } of the item, if it has one
         * 
         * @return UsableCapability or null
         */
        public InteractableCapability getInteractableCapability();

        /**
         * Returns the {@link com.lhf.game.item.ItemContainerCapability Item Container
         * Capability } of the item, if it has one
         * 
         * @return ItemContainerCapability or null
         */
        public ItemContainerCapability getItemContainerCapability();

        /**
         * Returns the {@link com.lhf.game.item.CreatureContainerCapability Creature
         * Container Capability } of the item, if it has one
         * 
         * @return CreatureContainerCapability or null
         */
        public CreatureContainerCapability getCreatureContainerCapability();

        /**
         * Returns the {@link com.lhf.game.item.GameEventProcessorCapability Game Event
         * Processor Capability } of the item, if it has one
         * 
         * @return GameEventProcessorCapability or null
         */
        public GameEventProcessorCapability getGameEventProcessorCapability();

        /**
         * Builds the IItem as intended
         * 
         * @return the new IItem
         */
        public IItem build();
    }

    /**
     * Returns the {@link com.lhf.game.item.RenameCapability Renaming Capability }
     * of the item, if it has one
     * 
     * @return RenameCapability or null
     */
    public RenameCapability getRenameCapability();

    /**
     * Returns the {@link com.lhf.game.item.UsableCapability Usable Capability } of
     * the item, if it has one
     * 
     * @return UsableCapability or null
     */
    public UsableCapability getUsableCapability();

    /**
     * Returns the {@link com.lhf.game.item.LockingCapability Locking Capability }
     * of the item, if it has one
     * 
     * @return LockingCapability or null
     */
    public LockingCapability getLockingCapability();

    /**
     * Returns the {@link com.lhf.game.item.UsableCapability Usable Capability } of
     * the item, if it has one
     * 
     * @return UsableCapability or null
     */
    public EquipableCapability getEquipableCapability();

    /**
     * Returns the {@link com.lhf.game.item.WeaponCapability Equipable Capability }
     * of the item, if it has one
     * 
     * @return WeaponCapability or null
     */
    public WeaponCapability getWeaponCapability();

    /**
     * Returns the {@link com.lhf.game.item.InteractableCapability Interactable
     * Capability } of the item, if it has one
     * 
     * @return UsableCapability or null
     */
    public InteractableCapability getInteractableCapability();

    /**
     * Returns the {@link com.lhf.game.item.ItemContainerCapability Item Container
     * Capability } of the item, if it has one
     * 
     * @return ItemContainerCapability or null
     */
    public ItemContainerCapability getItemContainerCapability();

    /**
     * Returns the {@link com.lhf.game.item.CreatureContainerCapability Creature
     * Container Capability } of the item, if it has one
     * 
     * @return CreatureContainerCapability or null
     */
    public CreatureContainerCapability getCreatureContainerCapability();

    /**
     * Returns the {@link com.lhf.game.item.GameEventProcessorCapability Game Event
     * Processor Capability } of the item, if it has one
     * 
     * @return GameEventProcessorCapability or null
     */
    public GameEventProcessorCapability getGameEventProcessorCapability();

}