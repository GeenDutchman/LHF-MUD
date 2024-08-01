package com.lhf.game.item;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

public class Item implements IItem {

    private final ItemID itemID;
    // Class name for discrimination
    private final String className;
    // Name it will be known by
    private final String objectName;
    // Will not output with look if false
    private boolean visible;
    // Every item should describe itself
    protected String descriptionString;
    // Describes if the item is moveable/takeable
    private final boolean takeable;
    // The Tag name
    private final String tagName;
    private final RenameCapability renameCapability;
    private final UsableCapability usableCapability;
    private final LockingCapability lockingCapability;
    private final EquipableCapability equipableCapability;
    private final WeaponCapability weaponCapability;
    private final InteractableCapability interactableCapability;
    private final ItemContainerCapability itemContainerCapability;
    private final CreatureContainerCapability creatureContainerCapability;
    private final GameEventProcessorCapability gameEventProcessorCapability;

    public static class ItemBuilder implements IItem.IItemBuilder {
        private String name = "item";
        private String descriptionString = "An item.";
        private boolean visible = true;
        private boolean takeable;
        private String customTagName;
        private RenameCapability.Renameable renameableCapability;
        private UsableCapability.Usable.UsableBuilder usableCapability;
        private LockingCapability.Locking.Builder lockingCapability;
        private EquipableCapability.Equipable.EquipableBuilder equipableCapability;
        private WeaponCapability.Weapon.WeaponBuilder weaponCapability;
        private InteractableCapability.Interactable.Builder interactableCapability;
        private ItemContainerCapability.Container.Builder itemContainerCapability;
        private CreatureContainerCapability.CreaturePen.Builder creatureContainerCapability;
        private GameEventProcessorCapability.Reactor.Builder gameEventProcessorCapability;

        @Override
        public ItemBuilder reset() {
            this.name = "item";
            this.descriptionString = "An item.";
            this.visible = true;
            this.takeable = false;
            this.customTagName = null;
            this.usableCapability = null;
            this.lockingCapability = null;
            this.equipableCapability = null;
            this.weaponCapability = null;
            this.interactableCapability = null;
            this.itemContainerCapability = null;
            this.creatureContainerCapability = null;
            this.gameEventProcessorCapability = null;
            return this;
        }

        public String getName() {
            return name == null ? "item" : name;
        }

        public ItemBuilder applyTemplate(ItemTemplate template) {
            if (template != null) {
                return template.applyTemplate(this);
            }
            return this;
        }

        public ItemBuilder setName(String naming) {
            if (naming == null) {
                throw new IllegalArgumentException("Item name cannot be null");
            } else if (!naming.matches(IItem.ITEM_NAMES)) {
                throw new IllegalArgumentException(
                        String.format("Item name must match the regex '%s', but was '%s'", IItem.ITEM_NAMES, naming));
            }
            this.name = naming.trim();
            return this;
        }

        public String getDescriptionString() {
            return descriptionString;
        }

        public ItemBuilder setDescriptionString(String descriptionString) {
            this.descriptionString = descriptionString;
            return this;
        }

        public boolean isVisible() {
            return visible;
        }

        public ItemBuilder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public boolean isTakeable() {
            return takeable;
        }

        public ItemBuilder setTakeable(boolean takeable) {
            this.takeable = takeable;
            return this;
        }

        @Override
        public String getTagName() {
            if (customTagName != null && !customTagName.isBlank()) {
                return customTagName;
            }
            String tag = "item";
            if (this.takeable) {
                tag = "takeable";
            }
            if (this.usableCapability != null) {
                tag = "usable";
            }
            if (this.equipableCapability != null) {
                tag = "equipable";
            }
            if (this.weaponCapability != null) {
                tag = "weapon";
            }
            if (this.interactableCapability != null) {
                tag = "interactable";
            }
            return tag;
        }

        public String getCustomTagName() {
            return customTagName;
        }

        public ItemBuilder setCustomTagName(String customTagName) {
            this.customTagName = customTagName;
            return this;
        }

        public UsableCapability getUsableCapability() {
            return usableCapability == null ? null : usableCapability.build();
        }

        public ItemBuilder setUsableCapability(UsableCapability.Usable.UsableBuilder usableCapability) {
            this.usableCapability = usableCapability;
            return this;
        }

        public ItemBuilder adjustUsableCapability(
                Function<UsableCapability.Usable.UsableBuilder, UsableCapability.Usable.UsableBuilder> adjustor) {
            if (adjustor != null) {
                if (this.usableCapability == null) {
                    this.usableCapability = new UsableCapability.Usable.UsableBuilder();
                }
                this.usableCapability = adjustor.apply(usableCapability);
            }
            return this;
        }

        public LockingCapability getLockingCapability() {
            return lockingCapability == null ? null : lockingCapability.build();
        }

        public ItemBuilder setLockingCapability(LockingCapability.Locking.Builder lockingCapability) {
            this.lockingCapability = lockingCapability;
            return this;
        }

        public ItemBuilder adjustLockingCapability(
                Function<LockingCapability.Locking.Builder, LockingCapability.Locking.Builder> adjustor) {
            if (adjustor != null) {
                if (this.lockingCapability == null) {
                    this.lockingCapability = new LockingCapability.Locking.Builder();
                }
                this.lockingCapability = adjustor.apply(lockingCapability);
            }
            return this;
        }

        public EquipableCapability getEquipableCapability() {
            return equipableCapability == null ? null : equipableCapability.build();
        }

        public ItemBuilder setEquipableCapability(EquipableCapability.Equipable.EquipableBuilder equipableCapability) {
            this.equipableCapability = equipableCapability;
            return this;
        }

        public ItemBuilder adjustEquipableCapability(
                Function<EquipableCapability.Equipable.EquipableBuilder, EquipableCapability.Equipable.EquipableBuilder> adjustor) {
            if (adjustor != null) {
                if (this.equipableCapability == null) {
                    this.equipableCapability = new EquipableCapability.Equipable.EquipableBuilder(null);
                }
                this.equipableCapability = adjustor.apply(equipableCapability);
            }
            return this;
        }

        public WeaponCapability getWeaponCapability() {
            return weaponCapability == null ? null : weaponCapability.build();
        }

        public ItemBuilder setWeaponCapability(WeaponCapability.Weapon.WeaponBuilder weaponCapability) {
            this.weaponCapability = weaponCapability;
            return this;
        }

        public ItemBuilder adjustWeaponCapability(
                Function<WeaponCapability.Weapon.WeaponBuilder, WeaponCapability.Weapon.WeaponBuilder> adjustor) {
            if (adjustor != null) {
                if (this.weaponCapability == null) {
                    this.weaponCapability = new WeaponCapability.Weapon.WeaponBuilder();
                }
                this.weaponCapability = adjustor.apply(weaponCapability);
            }
            return this;
        }

        public InteractableCapability getInteractableCapability() {
            return interactableCapability == null ? null : interactableCapability.build();
        }

        public ItemBuilder setInteractableCapability(
                InteractableCapability.Interactable.Builder interactableCapability) {
            this.interactableCapability = interactableCapability;
            return this;
        }

        public ItemBuilder adjustInteractableCapability(
                Function<InteractableCapability.Interactable.Builder, InteractableCapability.Interactable.Builder> adjustor) {
            if (adjustor != null) {
                if (this.interactableCapability == null) {
                    this.interactableCapability = new InteractableCapability.Interactable.Builder();
                }
                this.interactableCapability = adjustor.apply(interactableCapability);
            }
            return this;
        }

        public ItemContainerCapability getItemContainerCapability() {
            return itemContainerCapability == null ? null : itemContainerCapability.build();
        }

        public ItemBuilder setItemContainerCapability(
                ItemContainerCapability.Container.Builder itemContainerCapability) {
            this.itemContainerCapability = itemContainerCapability;
            return this;
        }

        public ItemBuilder adjustItemContainerCapability(
                Function<ItemContainerCapability.Container.Builder, ItemContainerCapability.Container.Builder> adjustor) {
            if (adjustor != null) {
                if (this.itemContainerCapability == null) {
                    this.itemContainerCapability = new ItemContainerCapability.Container.Builder();
                }
                this.itemContainerCapability = adjustor.apply(itemContainerCapability);
            }
            return this;
        }

        public CreatureContainerCapability getCreatureContainerCapability() {
            return creatureContainerCapability == null ? null : creatureContainerCapability.build();
        }

        public ItemBuilder setCreatureContainerCapability(
                CreatureContainerCapability.CreaturePen.Builder creatureContainerCapability) {
            this.creatureContainerCapability = creatureContainerCapability;
            return this;
        }

        public ItemBuilder adjustCreatureContainerCapability(
                Function<CreatureContainerCapability.CreaturePen.Builder, CreatureContainerCapability.CreaturePen.Builder> adjustor) {
            if (adjustor != null) {
                if (this.creatureContainerCapability == null) {
                    this.creatureContainerCapability = CreatureContainerCapability.CreaturePen.getBuilder();
                }
                this.creatureContainerCapability = adjustor.apply(creatureContainerCapability);
            }
            return this;
        }

        public GameEventProcessorCapability getGameEventProcessorCapability() {
            return gameEventProcessorCapability == null ? null : gameEventProcessorCapability.build();
        }

        public ItemBuilder setGameEventProcessorCapability(
                GameEventProcessorCapability.Reactor.Builder gameEventProcessorCapability) {
            this.gameEventProcessorCapability = gameEventProcessorCapability;
            return this;
        }

        public ItemBuilder adjustGameEventProcessorCapability(
                Function<GameEventProcessorCapability.Reactor.Builder, GameEventProcessorCapability.Reactor.Builder> adjustor) {
            if (adjustor != null) {
                if (this.gameEventProcessorCapability == null) {
                    this.gameEventProcessorCapability = new GameEventProcessorCapability.Reactor.Builder();
                }
                this.gameEventProcessorCapability = adjustor.apply(gameEventProcessorCapability);
            }
            return this;
        }

        public RenameCapability getRenameableCapability() {
            return renameableCapability;
        }

        public ItemBuilder setRenameableCapability(RenameCapability.Renameable renameableCapability) {
            this.renameableCapability = renameableCapability;
            return this;
        }

        @Override
        public RenameCapability getRenameCapability() {
            return this.renameableCapability;
        }

        public ItemBuilder adjustRenameableCapability(Function<RenameCapability, RenameCapability> adjustor) {
            if (adjustor != null) {
                if (this.renameableCapability == null) {
                    this.renameableCapability = new RenameCapability.Renameable();
                }
                this.renameableCapability = adjustor.apply(renameableCapability);
            }
            return this;
        }

        @Override
        public Item build() {
            return new Item(this);
        }

    }

    public Item(ItemBuilder builder) {
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        if (builder == null) {
            this.objectName = "item";
            this.visible = true;
            this.descriptionString = "An item.";
            this.takeable = false;
            this.tagName = "item";
            this.renameCapability = null;
            this.usableCapability = null;
            this.lockingCapability = null;
            this.equipableCapability = null;
            this.weaponCapability = null;
            this.interactableCapability = null;
            this.itemContainerCapability = null;
            this.creatureContainerCapability = null;
            this.gameEventProcessorCapability = null;
        } else {
            this.objectName = builder.getName();
            this.visible = builder.isVisible();
            this.descriptionString = builder.getDescriptionString();
            this.takeable = builder.isTakeable();
            this.tagName = builder.getTagName();
            this.renameCapability = builder.getRenameCapability();
            this.usableCapability = builder.getUsableCapability();
            this.lockingCapability = builder.getLockingCapability();
            this.equipableCapability = builder.getEquipableCapability();
            this.weaponCapability = builder.getWeaponCapability();
            this.interactableCapability = builder.getInteractableCapability();
            this.itemContainerCapability = builder.getItemContainerCapability();
            this.creatureContainerCapability = builder.getCreatureContainerCapability();
            this.gameEventProcessorCapability = builder.getGameEventProcessorCapability();
        }
    }

    public Item(String name) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = this.objectName;
    }

    public Item(String name, String description) {
        assert name.trim().length() >= 3;
        this.itemID = new ItemID();
        this.className = this.getClass().getName();
        this.objectName = name.trim();
        this.visible = true;
        this.descriptionString = description;
    }

    protected Item(ItemID itemID, String className, String name, boolean visible, String descriptionString) {
        if (itemID == null) {
            throw new IllegalArgumentException("item id cannot be null");
        } else if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException(
                    String.format("name argument cannot be null or have a length shorter than 3: %s", name));
        } else if (className == null) {
            throw new IllegalArgumentException("object name is critical and cannot be null!");
        }
        this.itemID = itemID;
        this.objectName = name;
        this.className = className;
        this.visible = visible;
        this.descriptionString = descriptionString;
    }

    @Override
    public ItemID getItemID() {
        return this.itemID;
    }

    @Override
    public Item makeCopy() {
        return new Item(new ItemID(), className, className, visible, descriptionString);
    }

    @Override
    public void acceptItemVisitor(ItemVisitor visitor) {
        if (visitor != null) {
            visitor.visit(this);
        }
    }

    @Override
    public final String getClassName() {
        return this.className;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public boolean checkName(String name) {
        return this.getName().equalsIgnoreCase(name.trim());
    }

    @Override
    public boolean CheckNameRegex(String possName, Integer minimumLength) {
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
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ro = (Item) obj;
        if (objectName.equals(ro.objectName)) {
            return this.itemID.equals(ro.itemID);
        }
        return false;
    }

    @Override
    public String getTagName() {
        return this.tagName;
    }

    @Override
    public String getDescription() {
        return this.descriptionString;
    }

    public String getDescriptionString() {
        return descriptionString;
    }

    public void setDescriptionString(String descriptionString) {
        this.descriptionString = descriptionString;
    }

    public boolean isTakeable() {
        return takeable;
    }

    public RenameCapability getRenameCapability() {
        return renameCapability;
    }

    public UsableCapability getUsableCapability() {
        return usableCapability;
    }

    public LockingCapability getLockingCapability() {
        return lockingCapability;
    }

    public EquipableCapability getEquipableCapability() {
        return equipableCapability;
    }

    public WeaponCapability getWeaponCapability() {
        return weaponCapability;
    }

    public InteractableCapability getInteractableCapability() {
        return interactableCapability;
    }

    public ItemContainerCapability getItemContainerCapability() {
        return itemContainerCapability;
    }

    public CreatureContainerCapability getCreatureContainerCapability() {
        return creatureContainerCapability;
    }

    public GameEventProcessorCapability getGameEventProcessorCapability() {
        return gameEventProcessorCapability;
    }

}
