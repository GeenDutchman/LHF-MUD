package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.messages.events.ItemEquippedEvent;
import com.lhf.messages.events.ItemUnequippedEvent;
import com.lhf.messages.events.ItemEquippedEvent.EquipResultType;
import com.lhf.messages.events.ItemUnequippedEvent.UnequipResultType;
import com.lhf.messages.events.SeeEvent.ABuilder;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public interface EquipableCapability extends ItemCapability {
    public List<EquipmentTypes> getEquipmentTypes();

    public List<EquipmentSlots> getEquipmentSlots();

    public List<ItemEffectSource> getEquipChanges();

    public List<ItemEffectSource> getUnequipChanges();

    public List<CreatureEffectSource> getEquipEffects();

    public List<CreatureEffectSource> getHiddenEquipEffects();

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.EQUIPABLE;
    }

    @Override
    default boolean isStateful() {
        return false;
    }

    @Override
    public default void describe(ABuilder<?> seeEventBuilder) {
        if (seeEventBuilder == null) {
            return;
        }
        final List<CreatureEffectSource> effects = this.getEquipEffects();
        if (effects != null) {
            for (final CreatureEffectSource creatureEffectSource : effects) {
                seeEventBuilder.addEffector(creatureEffectSource);
            }
        }
        final List<EquipmentSlots> mySlots = this.getEquipmentSlots();
        if (mySlots != null) {
            for (final EquipmentSlots equipmentSlots : mySlots) {
                seeEventBuilder.addSeen(SeeCategory.EQUIPMENT_SLOTS, equipmentSlots);
            }
        }
        final List<EquipmentTypes> myTypes = this.getEquipmentTypes();
        if (myTypes != null) {
            for (final EquipmentTypes equipmentTypes : myTypes) {
                seeEventBuilder.addSeen(SeeCategory.PROFICIENCIES, equipmentTypes);
            }
        }
    }

    public static void onEquippedBy(ICreature equipper, IItem thisItem) {
        if (equipper == null) {
            return;
        }
        if (thisItem == null) {
            ICreature.eventAccepter.accept(equipper, ItemEquippedEvent.getBuilder().setNotBroadcast().setItem(null)
                    .setSubType(EquipResultType.NOTEQUIPBLE).Build());
            return;
        }
        final EquipableCapability capability = thisItem.getEquipableCapability();
        if (capability == null) {
            ICreature.eventAccepter.accept(equipper, ItemEquippedEvent.getBuilder().setNotBroadcast().setItem(thisItem)
                    .setSubType(EquipResultType.NOTEQUIPBLE).Build());
            return;
        }
        final List<CreatureEffectSource> equipEffects = capability.getEquipEffects();
        if (equipEffects != null) {
            for (CreatureEffectSource effector : equipEffects) {
                ICreature.eventAccepter.accept(equipper,
                        equipper.applyEffect(new CreatureEffect(effector, equipper, thisItem)));
            }
        }
        final List<CreatureEffectSource> hiddenEffects = capability.getHiddenEquipEffects();
        if (hiddenEffects != null) {
            for (CreatureEffectSource effector : hiddenEffects) {
                ICreature.eventAccepter.accept(equipper,
                        equipper.applyEffect(new CreatureEffect(effector, equipper, thisItem)));
            }
        }
        final List<ItemEffectSource> equipChanges = capability.getEquipChanges();
        if (equipChanges != null) {
            for (ItemEffectSource effector : equipChanges) {
                ICreature.eventAccepter.accept(equipper,
                        thisItem.applyEffect(new ItemEffect(effector, equipper, thisItem)));
            }
        }
    }

    public static void onUnequippedBy(ICreature unequipper, IItem thisItem) {
        if (unequipper == null) {
            return;
        }
        if (thisItem == null) {
            ICreature.eventAccepter.accept(unequipper, ItemUnequippedEvent.getBuilder().setNotBroadcast().setItem(null)
                    .setSubType(UnequipResultType.ITEM_NOT_EQUIPPED).Build());
            return;
        }
        final EquipableCapability capability = thisItem.getEquipableCapability();
        if (capability == null) {
            ICreature.eventAccepter.accept(unequipper, ItemUnequippedEvent.getBuilder().setNotBroadcast()
                    .setItem(thisItem).setSubType(UnequipResultType.ITEM_NOT_EQUIPPED).Build());
            return;
        }
        final List<CreatureEffectSource> equipEffects = capability.getEquipEffects();
        if (equipEffects != null) {
            for (CreatureEffectSource effector : equipEffects) {
                if (effector.getPersistence() != null
                        && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                    ICreature.eventAccepter.accept(unequipper, unequipper.repealEffect(effector.getName()));
                }
            }
        }
        final List<CreatureEffectSource> hiddenEffects = capability.getHiddenEquipEffects();
        if (hiddenEffects != null) {
            for (CreatureEffectSource effector : hiddenEffects) {
                if (effector.getPersistence() != null
                        && TickType.CONDITIONAL.equals(effector.getPersistence().getTickSize())) {
                    ICreature.eventAccepter.accept(unequipper, unequipper.repealEffect(effector.getName()));
                }
            }
        }
        final List<ItemEffectSource> unequipChanges = capability.getUnequipChanges();
        if (unequipChanges != null) {
            for (ItemEffectSource effector : unequipChanges) {
                ICreature.eventAccepter.accept(unequipper,
                        thisItem.applyEffect(new ItemEffect(effector, unequipper, thisItem)));
            }
        }
    }

    public static EquipableCapability generateEquipableCapability() {
        return new Equipable();
    }

    public static final class Equipable implements EquipableCapability {
        private final List<EquipmentTypes> equipmentTypes;
        private final List<EquipmentSlots> equipmentSlots;
        private final List<ItemEffectSource> equipChanges;
        private final List<ItemEffectSource> unequipChanges;
        private final List<CreatureEffectSource> equipEffects;
        private final List<CreatureEffectSource> hiddenEquipEffects;

        public static class EquipableBuilder {
            private List<EquipmentTypes> equipmentTypes;
            private List<EquipmentSlots> equipmentSlots;
            private List<ItemEffectSource.Builder> equipChanges;
            private List<ItemEffectSource.Builder> unequipChanges;
            private List<CreatureEffectSource.Builder> equipEffects;
            private List<CreatureEffectSource.Builder> hiddenEquipEffects;

            public EquipableBuilder(EquipmentSlots slot) {
                this.equipmentSlots = new ArrayList<>();
                if (slot != null) {
                    this.equipmentSlots.add(slot);
                }
            }

            public List<EquipmentTypes> getEquipmentTypes() {
                return equipmentTypes;
            }

            public EquipableBuilder setEquipmentTypes(List<EquipmentTypes> equipmentTypes) {
                this.equipmentTypes = equipmentTypes;
                return this;
            }

            public EquipableBuilder addEquipmentType(EquipmentTypes type) {
                if (type != null) {
                    if (this.equipmentTypes == null) {
                        this.equipmentTypes = new ArrayList<>();
                    }
                    this.equipmentTypes.add(type);
                }
                return this;
            }

            public EquipableBuilder clearEquipmentTypes() {
                if (this.equipmentTypes != null) {
                    this.equipmentTypes.clear();
                }
                return this;
            }

            public List<EquipmentSlots> getEquipmentSlots() {
                return equipmentSlots;
            }

            public EquipableBuilder setEquipmentSlots(List<EquipmentSlots> equipmentSlots) {
                this.equipmentSlots = equipmentSlots;
                return this;
            }

            public EquipableBuilder addEquipmentSlot(EquipmentSlots slot) {
                if (slot != null) {
                    if (this.equipmentSlots == null) {
                        this.equipmentSlots = new ArrayList<>();
                    }
                    this.equipmentSlots.add(slot);
                }
                return this;
            }

            public EquipableBuilder clearEquipmentSlots() {
                if (this.equipmentSlots != null) {
                    this.equipmentSlots.clear();
                }
                return this;
            }

            public List<ItemEffectSource> getEquipChanges() {
                return equipChanges == null ? null
                        : this.equipChanges.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .toList();
            }

            public EquipableBuilder setEquipChanges(List<ItemEffectSource.Builder> equipChanges) {
                this.equipChanges = equipChanges;
                return this;
            }

            public EquipableBuilder addEquipChange(ItemEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.equipChanges == null) {
                        this.equipChanges = new ArrayList<>();
                    }
                    this.equipChanges.add(builder);
                }
                return this;
            }

            public EquipableBuilder clearEquipChanges() {
                if (this.equipChanges != null) {
                    this.equipChanges.clear();
                }
                return this;
            }

            public List<ItemEffectSource> getUnquipChanges() {
                return unequipChanges == null ? null
                        : this.unequipChanges.stream().filter(builder -> builder != null)
                                .map(builder -> builder.build()).toList();
            }

            public EquipableBuilder setUnequipChanges(List<ItemEffectSource.Builder> unequipChanges) {
                this.unequipChanges = unequipChanges;
                return this;
            }

            public EquipableBuilder addUnequipChange(ItemEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.unequipChanges == null) {
                        this.unequipChanges = new ArrayList<>();
                    }
                    this.unequipChanges.add(builder);
                }
                return this;
            }

            public EquipableBuilder clearUnequipChanges() {
                if (this.unequipChanges != null) {
                    this.unequipChanges.clear();
                }
                return this;
            }

            public List<CreatureEffectSource> getEquipEffects() {
                return equipEffects == null ? null
                        : this.equipEffects.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .toList();
            }

            public EquipableBuilder setEquipEffects(List<CreatureEffectSource.Builder> equipEffects) {
                this.equipEffects = equipEffects;
                return this;
            }

            public EquipableBuilder addEquipEffect(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.equipEffects == null) {
                        this.equipEffects = new ArrayList<>();
                    }
                    this.equipEffects.add(builder);
                }
                return this;
            }

            public EquipableBuilder clearEquipEffects() {
                if (this.equipEffects != null) {
                    this.equipEffects.clear();
                }
                return this;
            }

            public List<CreatureEffectSource> getHiddenEquipEffects() {
                return hiddenEquipEffects == null ? null
                        : this.hiddenEquipEffects.stream().filter(builder -> builder != null)
                                .map(builder -> builder.build()).toList();
            }

            public EquipableBuilder setHiddenEquipEffects(List<CreatureEffectSource.Builder> hiddenEquipEffects) {
                this.hiddenEquipEffects = hiddenEquipEffects;
                return this;
            }

            public EquipableBuilder addHiddenEquipEffect(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.hiddenEquipEffects == null) {
                        this.hiddenEquipEffects = new ArrayList<>();
                    }
                    this.hiddenEquipEffects.add(builder);
                }
                return this;
            }

            public EquipableBuilder clearHiddenEquipEffects() {
                if (this.hiddenEquipEffects != null) {
                    this.hiddenEquipEffects.clear();
                }
                return this;
            }

            public Equipable build() {
                if (this.equipmentSlots == null || this.equipmentSlots.isEmpty()) {
                    throw new IllegalStateException("To build an Equipable it must have a slot!");
                }
                return new Equipable(this);
            }

            @Override
            public String toString() {
                StringJoiner sj = new StringJoiner(", ", "EquipableBuilder [", "]");
                if (equipmentSlots != null && !equipmentSlots.isEmpty()) {
                    sj.add("equimentSlots=" + equipmentSlots.toString());
                }
                if (equipmentTypes != null && !equipmentTypes.isEmpty()) {
                    sj.add("equipmentTypes=" + equipmentTypes.toString());
                }
                if (equipChanges != null && !equipChanges.isEmpty()) {
                    sj.add("equipChanges=" + equipChanges.toString());
                }
                if (unequipChanges != null && !unequipChanges.isEmpty()) {
                    sj.add("unequipChanges=" + unequipChanges.toString());
                }
                if (equipEffects != null && !equipEffects.isEmpty()) {
                    sj.add("equipEffects=" + equipEffects.toString());
                }
                if (hiddenEquipEffects != null && !hiddenEquipEffects.isEmpty()) {
                    sj.add("hiddenEquipEffects=" + hiddenEquipEffects.toString());
                }
                return sj.toString();
            }

        }

        private Equipable() {
            this.equipmentTypes = List.of();
            this.equipmentSlots = List.of();
            this.equipChanges = List.of();
            this.unequipChanges = List.of();
            this.equipEffects = List.of();
            this.hiddenEquipEffects = List.of();
        }

        protected Equipable(EquipableBuilder builder) {
            if (builder == null) {
                this.equipmentTypes = List.of();
                this.equipmentSlots = List.of();
                this.equipChanges = List.of();
                this.unequipChanges = List.of();
                this.equipEffects = List.of();
                this.hiddenEquipEffects = List.of();
            } else {
                this.equipmentTypes = builder.getEquipmentTypes();
                this.equipmentSlots = builder.getEquipmentSlots();
                this.equipChanges = builder.getEquipChanges();
                this.unequipChanges = builder.getUnquipChanges();
                this.equipEffects = builder.getEquipEffects();
                this.hiddenEquipEffects = builder.getHiddenEquipEffects();
            }
        }

        @Override
        public List<EquipmentTypes> getEquipmentTypes() {
            return this.equipmentTypes == null ? null : Collections.unmodifiableList(this.equipmentTypes);
        }

        @Override
        public List<EquipmentSlots> getEquipmentSlots() {
            return this.equipmentSlots == null ? null : Collections.unmodifiableList(this.equipmentSlots);
        }

        @Override
        public List<ItemEffectSource> getEquipChanges() {
            return this.equipChanges == null ? null : Collections.unmodifiableList(this.equipChanges);
        }

        @Override
        public List<ItemEffectSource> getUnequipChanges() {
            return this.unequipChanges == null ? null : Collections.unmodifiableList(this.unequipChanges);
        }

        @Override
        public List<CreatureEffectSource> getEquipEffects() {
            return this.equipEffects == null ? null : Collections.unmodifiableList(this.equipEffects);
        }

        @Override
        public List<CreatureEffectSource> getHiddenEquipEffects() {
            return this.hiddenEquipEffects == null ? null : Collections.unmodifiableList(this.hiddenEquipEffects);
        }

        @Override
        public int hashCode() {
            return Objects.hash(equipmentTypes, equipmentSlots, equipChanges, unequipChanges, equipEffects,
                    hiddenEquipEffects);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Equipable))
                return false;
            Equipable other = (Equipable) obj;
            return Objects.equals(equipmentTypes, other.equipmentTypes)
                    && Objects.equals(equipmentSlots, other.equipmentSlots)
                    && Objects.equals(equipChanges, other.equipChanges)
                    && Objects.equals(unequipChanges, other.unequipChanges)
                    && Objects.equals(equipEffects, other.equipEffects)
                    && Objects.equals(hiddenEquipEffects, other.hiddenEquipEffects);
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "EquipableBuilder [", "]");
            if (equipmentSlots != null && !equipmentSlots.isEmpty()) {
                sj.add("equimentSlots=" + equipmentSlots.toString());
            }
            if (equipmentTypes != null && !equipmentTypes.isEmpty()) {
                sj.add("equipmentTypes=" + equipmentTypes.toString());
            }
            if (equipChanges != null && !equipChanges.isEmpty()) {
                sj.add("equipChanges=" + equipChanges.toString());
            }
            if (unequipChanges != null && !unequipChanges.isEmpty()) {
                sj.add("unequipChanges=" + unequipChanges.toString());
            }
            if (equipEffects != null && !equipEffects.isEmpty()) {
                sj.add("equipEffects=" + equipEffects.toString());
            }
            if (hiddenEquipEffects != null && !hiddenEquipEffects.isEmpty()) {
                sj.add("hiddenEquipEffects=" + hiddenEquipEffects.toString());
            }
            return sj.toString();
        }

    }

}
