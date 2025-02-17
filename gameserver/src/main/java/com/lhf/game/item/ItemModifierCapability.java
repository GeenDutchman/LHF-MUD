package com.lhf.game.item;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.RichOutput;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.IExternalReference;
import com.lhf.game.IExternalReference.ExternalReference;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.item.EffectorCapability.EffectorKernel.EffectorKernelBuilder;
import com.lhf.game.item.ItemEffectSource.Builder;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface ItemModifierCapability extends EffectorCapability<IItem, ItemEffectSource> {

    // TODO: new MODIFY command

    @Override
    public ItemModifierCapability adjustUses(int delta);

    @Override
    public ItemModifierCapability setUses(int uses);

    @Override
    public ItemFilterQuery getTargetingRestrictions();

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.MODIFIER;
    }

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    public default ItemUsedEvent.Builder getUsedEventBuilder(CommandContext ctx, IItem myItem, IItem target) {
        return ItemUsedEvent.getBuilder().setUsable(myItem).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature()).editMessage(builder -> {
                    if (builder == null) {
                        return;
                    }
                    final RichOutput messages = this.getUseDisplay();
                    if (messages == null) {
                        return;
                    }
                    builder.appendRichOutput(messages);
                }).setItemUser(ctx.getCreature()).setTarget(target);
    }

    @Override
    public default void applyEffectsOnTarget(CommandContext ctx, IItem item, IItem target) {
        if (target == null || item == null) {
            return;
        }
        final Stream<ItemEffectSource> effects = this.getTargetEffects();
        if (effects == null) {
            return;
        }
        effects.filter(source -> source != null).forEachOrdered(source -> {
            final ItemEffect effect = new ItemEffect(source, ctx.getCreature(), item);
            this.broadcast(ctx, target.applyEffect(effect));
        });
    }

    @Override
    default void applyEffectsOnInternalTargets(CommandContext ctx, IItem myItem) {
        if (ctx == null || myItem == null) {
            return;
        }
        final Stream<IExternalReference<IItem>> internalTargets = this.getInternalTargetReferences();
        if (internalTargets == null) {
            return;
        }
        final Stream<ItemEffectSource> internalTargetEffects = this.getInternalTargetEffects();
        if (internalTargetEffects == null) {
            return;
        }
        internalTargets.filter(ref -> ref != null).map(ref -> ref.getReference()).filter(item -> item != null)
                .forEachOrdered(item -> {
                    internalTargetEffects.filter(source -> source != null).forEachOrdered(source -> {
                        final ItemEffect effect = new ItemEffect(source, ctx.getCreature(), myItem);
                        this.broadcast(ctx, item.applyEffect(effect));
                    });
                });
    }

    public static ItemModifierCapability generateModifierCapability() {
        return new ItemModifier();
    }

    public static final class ItemModifier implements ItemModifierCapability {
        private final EffectorKernel kernel;
        private final Set<ItemEffectSource> targetEffects;
        private final Set<ItemEffectSource> internalTargetEffects;
        private final Set<ExternalReference<IItem>> internalTargets;
        private final int totalNumberUsableTimes;
        private final ItemFilterQuery targetFilter;
        private int timesUsed = 0;

        private ItemModifier() {
            kernel = null;
            targetEffects = Set.of();
            internalTargetEffects = Set.of();
            internalTargets = Set.of();
            totalNumberUsableTimes = -1;
            targetFilter = null;
        }

        protected ItemModifier(ItemModifierBuilder builder) {
            if (builder == null) {
                kernel = null;
                targetEffects = Set.of();
                internalTargetEffects = Set.of();
                internalTargets = Set.of();
                totalNumberUsableTimes = -1;
                targetFilter = null;
            } else {
                final EffectorKernelBuilder kernelBuilder = builder.getKernel();
                kernel = kernelBuilder != null ? kernelBuilder.build() : null;
                final Set<Builder> effectsBuilders = builder.getTargetEffects();
                targetEffects = effectsBuilders != null
                        ? effectsBuilders.stream().filter(b -> b != null).map(b -> b.build())
                                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                        Collections::unmodifiableSet))
                        : Set.of();
                final Set<Builder> internals = builder.getInternalTargetEffects();
                internalTargetEffects = internals != null
                        ? internals.stream().filter(b -> b != null).map(b -> b.build())
                                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                        Collections::unmodifiableSet))
                        : Set.of();
                final Set<ExternalReference<IItem>> refs = builder.getInternalTargets();
                internalTargets = refs != null ? new LinkedHashSet<>(refs) : Set.of();
                totalNumberUsableTimes = builder.getTotalNumberUsableTimes();
                targetFilter = builder.getTargetFilter();
                timesUsed = builder.getTimesUsed();
            }
        }

        public static final class ItemModifierBuilder {
            private EffectorKernel.EffectorKernelBuilder kernel = EffectorKernel.getBuilder();
            private Set<ItemEffectSource.Builder> targetEffects;
            private Set<ItemEffectSource.Builder> internalTargetEffects;
            private Set<ExternalReference<IItem>> internalTargets;
            private int totalNumberUsableTimes;
            private ItemFilterQuery targetFilter;
            private int timesUsed = 0;

            public EffectorKernel.EffectorKernelBuilder getKernel() {
                return kernel;
            }

            public ItemModifierBuilder setKernel(EffectorKernel.EffectorKernelBuilder kernel) {
                this.kernel = kernel;
                return this;
            }

            public ItemModifierBuilder adjustKernel(Consumer<EffectorKernel.EffectorKernelBuilder> adjustor) {
                if (adjustor != null) {
                    if (this.kernel == null) {
                        this.kernel = EffectorKernel.getBuilder();
                    }
                    adjustor.accept(kernel);
                }
                return this;
            }

            public Set<ItemEffectSource.Builder> getTargetEffects() {
                return targetEffects;
            }

            public ItemModifierBuilder setTargetEffects(Set<ItemEffectSource.Builder> targetEffects) {
                this.targetEffects = targetEffects;
                return this;
            }

            public Set<ItemEffectSource.Builder> getInternalTargetEffects() {
                return internalTargetEffects;
            }

            public ItemModifierBuilder setInternalTargetEffects(Set<ItemEffectSource.Builder> internalTargetEffects) {
                this.internalTargetEffects = internalTargetEffects;
                return this;
            }

            public ItemModifierBuilder addInternalTargetEffect(ItemEffectSource.Builder internalTargetEffect) {
                if (internalTargetEffect != null) {
                    if (this.internalTargetEffects == null) {
                        this.internalTargetEffects = new LinkedHashSet<>();
                    }
                    this.internalTargetEffects.add(internalTargetEffect);
                }
                return this;
            }

            public Set<ExternalReference<IItem>> getInternalTargets() {
                return internalTargets;
            }

            public ItemModifierBuilder setInternalTargets(Set<ExternalReference<IItem>> internalTargets) {
                this.internalTargets = internalTargets;
                return this;
            }

            public ItemModifierBuilder addInternalTarget(ExternalReference<IItem> internalTarget) {
                if (internalTarget != null) {
                    if (this.internalTargets == null) {
                        this.internalTargets = new LinkedHashSet<>();
                    }
                    this.internalTargets.add(internalTarget);
                }
                return this;
            }

            public int getTotalNumberUsableTimes() {
                return totalNumberUsableTimes;
            }

            public ItemModifierBuilder setTotalNumberUsableTimes(int totalNumberUsableTimes) {
                this.totalNumberUsableTimes = totalNumberUsableTimes;
                return this;
            }

            public ItemFilterQuery getTargetFilter() {
                return targetFilter;
            }

            public ItemModifierBuilder setTargetFilter(ItemFilterQuery targetFilter) {
                this.targetFilter = targetFilter;
                return this;
            }

            public int getTimesUsed() {
                return timesUsed;
            }

            public ItemModifierBuilder setTimesUsed(int timesUsed) {
                this.timesUsed = timesUsed;
                return this;
            }

            public ItemModifier build() {
                return new ItemModifier(this);
            }

        }

        public EffectorKernel getKernel() {
            return kernel;
        }

        public Stream<ItemEffectSource> getTargetEffects() {
            return targetEffects != null ? targetEffects.stream() : Stream.of();
        }

        public Stream<ItemEffectSource> getInternalTargetEffects() {
            return internalTargetEffects != null ? internalTargetEffects.stream() : Stream.of();
        }

        public Set<ExternalReference<IItem>> getInternalTargets() {
            return internalTargets;
        }

        public int getTotalNumberUsableTimes() {
            return totalNumberUsableTimes;
        }

        @Override
        public ItemFilterQuery getTargetingRestrictions() {
            return targetFilter;
        }

        public int getTimesUsed() {
            return timesUsed;
        }

        @Override
        public ItemModifierCapability adjustUses(int delta) {
            this.timesUsed += delta;
            return this;
        }

        @Override
        public ItemModifierCapability setUses(int uses) {
            this.timesUsed = uses;
            return this;
        }

        @Override
        public Stream<IExternalReference<IItem>> getInternalTargetReferences() {
            return this.internalTargets != null
                    ? this.internalTargets.stream().filter(r -> r != null).map(r -> (IExternalReference<IItem>) r)
                    : Stream.of();
        }

        public final ItemFilterQuery getSelfRestrictions() {
            return kernel.getSelfRestrictions();
        }

        public final CreatureFilterQuery getUserRestrictions() {
            return kernel.getUserRestrictions();
        }

        public final Stream<ItemEffectSource> getSelfEffects() {
            return kernel.getSelfEffects();
        }

        public final Stream<CreatureEffectSource> getUserEffects() {
            return kernel.getUserEffects();
        }

        public final boolean isUseDisplayPaged() {
            return kernel.isUseDisplayPaged();
        }

        public boolean isEquippingRequired() {
            return kernel.isEquippingRequired();
        }

        public List<RichOutput> getUseDisplayPages() {
            return kernel.getUseDisplayPages();
        }

        @Override
        public synchronized boolean useOnce() {
            this.timesUsed++;
            return this.timesUsed < this.totalNumberUsableTimes;
        }

        @Override
        public void describe(ABuilder<?> seeEventBuilder) {
            if (seeEventBuilder == null) {
                return;
            }
            StringJoiner sj = new StringJoiner("\n").setEmptyValue("");
            sj.add("This item can be usable.");
            if (this.timesUsed != 0) {
                sj.add("This item looks to have been used before.");
            }
            if (this.isEquippingRequired()) {
                sj.add("It must be equipped if it is to be used.");
            }
            if (this.getSelfRestrictions() != null) {
                sj.add("This item has need of meeting requirements of its own before being used.");
            }
            if (this.getUserRestrictions() != null) {
                sj.add("The user must meet certain requirements before the user can use this item.");
            }
            if (this.getSelfEffects() != null || this.getSelfEffects().count() != 0) {
                sj.add("When used, it has the following effects upon itself:");
                this.getSelfEffects().forEachOrdered(source -> sj.add(source.getDescription()));
            }
            if (this.targetEffects != null || !this.targetEffects.isEmpty()) {
                sj.add("When used on a target Item, it has the following effects:");
                for (final ItemEffectSource source : this.targetEffects) {
                    sj.add(source.getDescription());
                }
            }
            if (this.internalTargets != null && !this.internalTargets.isEmpty() && this.internalTargetEffects != null
                    && !this.internalTargetEffects.isEmpty()) {
                sj.add("This item targets some other items and has some effect upon them.");
            }
            seeEventBuilder.addExtraInfo(sj.toString());

        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Usable [", "]");
            sj.add("kernel=" + this.kernel.toString());
            sj.add("timesUsed=" + Integer.toString(timesUsed));
            sj.add("totalNumberUsableTimes=" + Integer.toString(totalNumberUsableTimes));
            if (this.targetEffects != null) {
                sj.add("targetEffects=" + targetEffects.toString());
            }
            if (this.targetFilter != null) {
                sj.add("targetFilter=" + this.targetFilter.toString());
            }
            if (this.internalTargets != null) {
                sj.add("internalTargets=" + this.internalTargets.toString());
            }
            if (this.internalTargetEffects != null) {
                sj.add("internalTargetEffects=" + this.internalTargetEffects.toString());
            }

            return sj.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(kernel, targetEffects, internalTargetEffects, internalTargets, totalNumberUsableTimes,
                    targetFilter);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof ItemModifier))
                return false;
            ItemModifier other = (ItemModifier) obj;
            return Objects.equals(kernel, other.kernel) && Objects.equals(targetEffects, other.targetEffects)
                    && Objects.equals(internalTargetEffects, other.internalTargetEffects)
                    && Objects.equals(internalTargets, other.internalTargets)
                    && totalNumberUsableTimes == other.totalNumberUsableTimes
                    && Objects.equals(targetFilter, other.targetFilter);
        }

    }
}
