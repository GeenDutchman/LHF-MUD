package com.lhf.game.item;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.IExternalReference;
import com.lhf.game.IExternalReference.ExternalReference;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.ItemEffectSource.Builder;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface UsableCapability extends EffectorCapability<ICreature, CreatureEffectSource> {

    @Override
    public UsableCapability adjustUses(int uses);

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.USABLE;
    }

    @Override
    public default ItemUsedEvent.Builder getUsedEventBuilder(CommandContext ctx, IItem myItem, ICreature target) {
        return ItemUsedEvent.getBuilder().setUsable(myItem).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature())
                .addMessage(this.getTargetEffects() == null ? null : "Affects try to take hold.")
                .editMessage(builder -> {
                    if (builder == null) {
                        return;
                    }
                    final RichOutput messages = this.getUseDisplay();
                    if (messages == null) {
                        return;
                    }
                    builder.appendRichOutput(messages);
                }).setTarget(target);
    }

    @Override
    default boolean customChecks(CommandContext ctx, IItem myItem, ICreature target) {
        if (ctx == null || target == null) {
            return false;
        }
        if (ctx.getSubAreaForSort(SubAreaSort.BATTLE) != null) {
            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm.hasCreature(target) && !bm.hasCreature(ctx.getCreature())) {
                // give out of turn message
                bm.addCreature(ctx.getCreature());
                ctx.receive(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.REJECTED).setNotBroadcast()
                        .Build());
                return false;
            }
        }
        final ICreature creature = ctx.getCreature();
        if (creature == null || myItem == null) {
            return false;
        }
        if (this.isEquippingRequired() && !creature.hasItem(myItem)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setSubType(UseOutMessageOption.REQUIRE_EQUIPPED));
            }
            return false;
        }
        if (this.isSelfOnly() && !target.equals(creature)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setSubType(UseOutMessageOption.CANNOT)
                        .addMessage("You can only target yourself!"));
            }
            return false;
        }
        return true;
    }

    @Override
    public default void applyEffectsOnTarget(CommandContext ctx, IItem item, ICreature creature) {
        if (creature == null || item == null) {
            return;
        }
        final Stream<CreatureEffectSource> effects = this.getTargetEffects();
        if (effects == null) {
            return;
        }
        effects.filter(source -> source != null).forEachOrdered(source -> {
            final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), item);
            this.broadcast(ctx, creature.applyEffect(effect));
        });
    }

    @Override
    default void applyEffectsOnInternalTargets(CommandContext ctx, IItem myItem) {
        if (ctx == null || myItem == null) {
            return;
        }
        final Stream<IExternalReference<ICreature>> internalTargets = this.getInternalTargetReferences();
        if (internalTargets == null) {
            return;
        }
        final Stream<CreatureEffectSource> internalTargetEffects = this.getInternalTargetEffects();
        if (internalTargetEffects == null) {
            return;
        }
        internalTargets.filter(ref -> ref != null).map(ref -> ref.getReference()).filter(creature -> creature != null)
                .forEachOrdered(creature -> {
                    internalTargetEffects.filter(source -> source != null).forEachOrdered(source -> {
                        final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), myItem);
                        this.broadcast(ctx, creature.applyEffect(effect));
                    });
                });
    }

    public static UsableCapability generateUsableCapability() {
        return new UsableCapability.Usable();
    }

    public static enum Delta implements Consumer<UsableCapability> {
        RESET_COUNT {
            @Override
            public void accept(UsableCapability arg0) {
                if (arg0 != null) {
                    final int count = arg0.getTimesUsed();
                    arg0.adjustUses(count * -1);
                }
            }
        },
        INCREMENT {
            @Override
            public void accept(UsableCapability arg0) {
                if (arg0 != null) {
                    arg0.useOnce();
                }
            }
        },
        DECREMENT {
            @Override
            public void accept(UsableCapability arg0) {
                if (arg0 != null) {
                    final int count = arg0.getTimesUsed();
                    arg0.adjustUses(count - 1 < 0 ? 0 : count - 1);
                }
            }
        },
        NOOP {
            @Override
            public void accept(UsableCapability arg0) {
                // does nothing
            }
        };

        @Override
        public abstract void accept(UsableCapability arg0);

        public Delta invert() {
            switch (this) {
            case DECREMENT:
                return INCREMENT;
            case INCREMENT:
                return DECREMENT;
            case RESET_COUNT:
                return RESET_COUNT;
            case NOOP:
            default:
                return NOOP;

            }
        }
    }

    public boolean isSelfOnly();

    @Override
    public CreatureFilterQuery getTargetingRestrictions();

    public final class Usable implements UsableCapability {
        private final EffectorKernel kernel;
        private final Set<CreatureEffectSource> useOnCreatureEffects;
        private final Set<CreatureEffectSource> internalTargetEffects;
        private final Set<ExternalReference<ICreature>> internalTargets;
        private final int totalNumberUsableTimes;
        private final boolean selfOnly;
        private final CreatureFilterQuery creatureFilter;
        private int timesUsed = 0;

        public static UsableBuilder getBuilder() {
            return new UsableBuilder();
        }

        public static final class UsableBuilder {
            private EffectorKernel.EffectorKernelBuilder kernelBuilder = EffectorKernel.getBuilder();
            private Set<CreatureEffectSource.Builder> internalTargetEffects;
            private Set<CreatureEffectSource.Builder> useOnCreatureEffects;
            private Set<ExternalReference<ICreature>> internalTargets;
            private int totalNumberUsableTimes = -1;
            private boolean selfOnly;
            private CreatureFilterQuery creatureFilter;
            private int timesUsed = 0;

            public ItemFilterQuery getSelfRestrictions() {
                return kernelBuilder.getSelfRestrictions();
            }

            public UsableBuilder setSelfRestrictions(ItemFilterQuery selfRestrictions) {
                this.kernelBuilder.setSelfRestrictions(selfRestrictions);
                return this;
            }

            public UsableBuilder setUserRestrictions(CreatureFilterQuery userRestrictions) {
                this.kernelBuilder.setUserRestrictions(userRestrictions);
                return this;
            }

            public UsableBuilder setSelfEffects(Set<Builder> selfEffects) {
                this.kernelBuilder.setSelfEffects(selfEffects);
                return this;
            }

            public UsableBuilder addSelfEffects(Builder... sources) {
                this.kernelBuilder.addSelfEffects(sources);
                return this;
            }

            public UsableBuilder addSelfEffects(Collection<Builder> sources) {
                this.kernelBuilder.addSelfEffects(sources);
                return this;
            }

            public Set<com.lhf.game.creature.CreatureEffectSource.Builder> getUserEffects() {
                return kernelBuilder.getUserEffects();
            }

            public UsableBuilder setUserEffects(Set<com.lhf.game.creature.CreatureEffectSource.Builder> userEffects) {
                this.kernelBuilder.setUserEffects(userEffects);
                return this;
            }

            public UsableBuilder addUserEffects(com.lhf.game.creature.CreatureEffectSource.Builder... sources) {
                this.kernelBuilder.addUserEffects(sources);
                return this;
            }

            public UsableBuilder addUserEffects(
                    Collection<com.lhf.game.creature.CreatureEffectSource.Builder> sources) {
                this.kernelBuilder.addUserEffects(sources);
                return this;
            }

            public UsableBuilder setUseDisplayPages(List<RichOutputBuilder> useDisplayPages) {
                this.kernelBuilder.setUseDisplayPages(useDisplayPages);
                return this;
            }

            public UsableBuilder addUseDisplayPages(RichOutputBuilder... builders) {
                this.kernelBuilder.addUseDisplayPages(builders);
                return this;
            }

            public UsableBuilder addUseDisplayPages(Collection<RichOutputBuilder> builders) {
                this.kernelBuilder.addUseDisplayPages(builders);
                return this;
            }

            public boolean isUseDisplayPaged() {
                return kernelBuilder.isUseDisplayPaged();
            }

            public UsableBuilder setUseDisplayPaged(boolean useDisplayPaged) {
                this.kernelBuilder.setUseDisplayPaged(useDisplayPaged);
                return this;
            }

            public Set<CreatureEffectSource> getInternalTargetEffects() {
                return internalTargetEffects == null ? null
                        : this.internalTargetEffects.stream().filter(builder -> builder != null)
                                .map(builder -> builder.build()).collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public UsableBuilder setInternalTargetEffects(Set<CreatureEffectSource.Builder> internalEffects) {
                this.internalTargetEffects = internalEffects;
                return this;
            }

            public UsableBuilder addInternalTargetEffect(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.internalTargetEffects == null) {
                        this.internalTargetEffects = new LinkedHashSet<>();
                    }
                    this.internalTargetEffects.add(builder);
                }
                return this;
            }

            public UsableBuilder clearInternalTargetEffects() {
                if (this.internalTargetEffects != null) {
                    this.internalTargetEffects.clear();
                }
                return this;
            }

            public Set<CreatureEffectSource> getUseOnCreatureEffects() {
                return useOnCreatureEffects == null ? null
                        : this.useOnCreatureEffects.stream().filter(builder -> builder != null)
                                .map(builder -> builder.build()).collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public UsableBuilder setUseOnCreatureEffects(Set<CreatureEffectSource.Builder> useOnCreatureEffects) {
                this.useOnCreatureEffects = useOnCreatureEffects;
                return this;
            }

            public UsableBuilder addUseOnCreatureEffect(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.useOnCreatureEffects == null) {
                        this.useOnCreatureEffects = new LinkedHashSet<>();
                    }
                    this.useOnCreatureEffects.add(builder);
                }
                return this;
            }

            public UsableBuilder clearCreatureEffects() {
                if (this.useOnCreatureEffects != null) {
                    this.useOnCreatureEffects.clear();
                }
                return this;
            }

            public Set<ExternalReference<ICreature>> getInternalTargets() {
                return internalTargets == null ? null
                        : internalTargets.stream().filter(ref -> ref != null)
                                .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public UsableBuilder setInternalTargets(Set<ExternalReference<ICreature>> references) {
                this.internalTargets = references;
                return this;
            }

            public UsableBuilder addInternalTarget(ExternalReference<ICreature> ref) {
                if (ref != null) {
                    if (this.internalTargets == null) {
                        this.internalTargets = new LinkedHashSet<>();
                    }
                    this.internalTargets.add(ref);
                }
                return this;
            }

            public UsableBuilder clearInternalTargets() {
                if (this.internalTargets != null) {
                    this.internalTargets.clear();
                }
                return this;
            }

            public UsableBuilder addUseDisplayPage(String nextPage) {
                if (nextPage != null) {
                    RichOutput.RichOutputBuilder nextPageBuilder = new RichOutputBuilder().appendString(nextPage);
                    return this.addUseDisplayPages(nextPageBuilder);
                }
                return this;
            }

            public UsableBuilder addUseDisplayPage(Consumer<RichOutputBuilder> pageBuilder) {
                if (pageBuilder != null) {
                    RichOutputBuilder builder = new RichOutputBuilder();
                    this.addUseDisplayPages(builder);
                    pageBuilder.accept(builder);
                }
                return this;
            }

            public RichOutputBuilder createOrGetPage(int index) {
                return this.kernelBuilder.createOrGetPage(index);
            }

            public UsableBuilder createOrEditPage(int index, Consumer<RichOutputBuilder> pageEditor) {
                this.kernelBuilder.createOrEditPage(index, pageEditor);
                return this;
            }

            public UsableBuilder clearUseDisplayPages() {
                if (this.kernelBuilder.useDisplayPages != null) {
                    this.kernelBuilder.useDisplayPages.clear();
                }
                return this;
            }

            public int getTotalNumberUsableTimes() {
                return totalNumberUsableTimes;
            }

            public UsableBuilder setTotalNumberUsableTimes(int totalNumberUsableTimes) {
                this.totalNumberUsableTimes = totalNumberUsableTimes;
                return this;
            }

            public boolean isEquippingRequired() {
                return this.kernelBuilder.isEquippingRequired();
            }

            public UsableBuilder setEquippingRequired(boolean equippingRequired) {
                this.kernelBuilder.setEquippingRequired(equippingRequired);
                return this;
            }

            public boolean isSelfOnly() {
                return selfOnly;
            }

            public UsableBuilder setSelfOnly(boolean selfOnly) {
                this.selfOnly = selfOnly;
                return this;
            }

            public CreatureFilterQuery getCreatureFilter() {
                return creatureFilter;
            }

            public UsableBuilder setCreatureFilter(CreatureFilterQuery creatureFilter) {
                this.creatureFilter = creatureFilter;
                return this;
            }

            public int getTimesUsed() {
                return timesUsed;
            }

            public UsableBuilder setTimesUsed(int timesUsed) {
                this.timesUsed = timesUsed;
                return this;
            }

            public CreatureFilterQuery affectsCreaturesLike() {
                return this.creatureFilter;
            }

            public EffectorKernel getKernel() {
                if (this.kernelBuilder == null) {
                    this.kernelBuilder = EffectorKernel.getBuilder();
                }
                return kernelBuilder.build();
            }

            public Usable build() {
                return new Usable(this);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("UsableBuilder [kernelBuilder=").append(kernelBuilder).append(", internalTargetEffects=")
                        .append(internalTargetEffects).append(", useOnCreatureEffects=").append(useOnCreatureEffects)
                        .append(", internalTargets=").append(internalTargets).append(", totalNumberUsableTimes=")
                        .append(totalNumberUsableTimes).append(", selfOnly=").append(selfOnly)
                        .append(", creatureFilter=").append(creatureFilter).append(", timesUsed=").append(timesUsed)
                        .append("]");
                return builder.toString();
            }

        }

        Usable() {
            this.kernel = EffectorKernel.getBuilder().build();
            this.internalTargetEffects = null;
            this.totalNumberUsableTimes = -1;
            this.internalTargets = null;
            this.useOnCreatureEffects = null;
            this.timesUsed = 0;
            this.selfOnly = false;
            this.creatureFilter = null;
        }

        protected Usable(UsableBuilder builder) {
            if (builder == null) {
                this.kernel = EffectorKernel.getBuilder().build();
                this.internalTargetEffects = null;
                this.totalNumberUsableTimes = -1;
                this.internalTargets = null;
                this.useOnCreatureEffects = null;
                this.timesUsed = 0;
                this.selfOnly = false;
                this.creatureFilter = null;
            } else {
                this.kernel = builder.getKernel();
                this.internalTargetEffects = builder.getInternalTargetEffects();
                this.useOnCreatureEffects = builder.getUseOnCreatureEffects();
                this.internalTargets = builder.getInternalTargets();
                this.totalNumberUsableTimes = builder.getTotalNumberUsableTimes();
                this.selfOnly = builder.isSelfOnly();
                this.creatureFilter = builder.getCreatureFilter();
                this.timesUsed = builder.getTimesUsed();
            }
        }

        public Usable(UsableCapability usable) {
            if (usable == null) {
                this.kernel = EffectorKernel.getBuilder().build();
                this.internalTargetEffects = null;
                this.totalNumberUsableTimes = -1;
                this.internalTargets = null;
                this.useOnCreatureEffects = null;
                this.timesUsed = 0;
                this.selfOnly = false;
                this.creatureFilter = null;
            } else {
                Stream<CreatureEffectSource> internalStream = usable.getInternalTargetEffects();
                if (internalStream != null) {
                    this.internalTargetEffects = internalStream.filter(effect -> effect != null)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } else {
                    this.internalTargetEffects = null;
                }
                this.totalNumberUsableTimes = usable.getTotalNumberUsableTimes();
                Stream<IExternalReference<ICreature>> internalRefStream = usable.getInternalTargetReferences();
                if (internalRefStream != null) {
                    this.internalTargets = internalRefStream.filter(ref -> ref != null)
                            .map(ref -> new ICreature.CreatureReference(ref))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } else {
                    this.internalTargets = null;
                }
                Stream<CreatureEffectSource> effectStream = usable.getTargetEffects();
                if (effectStream != null) {
                    this.useOnCreatureEffects = effectStream.filter(effect -> effect != null)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } else {
                    this.useOnCreatureEffects = null;
                }
                this.timesUsed = 0;
                this.selfOnly = usable.isSelfOnly();
                this.creatureFilter = usable.getTargetingRestrictions();
                this.kernel = new EffectorKernel(usable.getSelfRestrictions(), usable.getUserRestrictions(),
                        usable.getSelfEffects(), usable.getUserEffects(), usable.getUseDisplayPages(),
                        usable.isUseDisplayPaged(), usable.isEquippingRequired());
            }
        }

        public EffectorKernel getEffectorKernel() {
            return this.kernel;
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
            if (this.selfOnly) {
                sj.add("The user can only target themselves with this item.");
            }
            if (this.getSelfEffects() != null || this.getSelfEffects().count() != 0) {
                sj.add("When used, it has the following effects upon itself:");
                this.getSelfEffects().forEachOrdered(source -> sj.add(source.getDescription()));
            }
            if (this.useOnCreatureEffects != null || !this.useOnCreatureEffects.isEmpty()) {
                sj.add("When used on a target Creature, it has the following effects:");
                for (final CreatureEffectSource source : this.useOnCreatureEffects) {
                    sj.add(source.getDescription());
                }
            }
            if (this.internalTargets != null && !this.internalTargets.isEmpty() && this.internalTargetEffects != null
                    && !this.internalTargetEffects.isEmpty()) {
                sj.add("This item targets some other creatures and has some effect upon them.");
            }
            seeEventBuilder.addExtraInfo(sj.toString());
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

        public List<RichOutput> getUseDisplayPages() {
            return kernel.getUseDisplayPages();
        }

        @Override
        public int getTotalNumberUsableTimes() {
            return this.totalNumberUsableTimes;
        }

        @Override
        public int getTimesUsed() {
            return this.timesUsed;
        }

        @Override
        public boolean isEquippingRequired() {
            return this.kernel.isEquippingRequired();
        }

        @Override
        public boolean isSelfOnly() {
            return this.selfOnly;
        }

        @Override
        public synchronized UsableCapability adjustUses(int uses) {
            this.timesUsed += uses;
            if (this.totalNumberUsableTimes > 0 && this.timesUsed > this.totalNumberUsableTimes) {
                this.timesUsed = this.totalNumberUsableTimes;
            }
            return this;
        }

        @Override
        public synchronized UsableCapability setUses(int count) {
            this.timesUsed = count;
            if (this.totalNumberUsableTimes > 0 && this.timesUsed > this.totalNumberUsableTimes) {
                this.timesUsed = this.totalNumberUsableTimes;
            }
            return this;
        };

        @Override
        public synchronized boolean useOnce() {
            this.timesUsed++;
            return this.timesUsed < this.totalNumberUsableTimes;
        }

        public Stream<CreatureEffectSource> getTargetEffects() {
            return this.useOnCreatureEffects != null ? this.useOnCreatureEffects.stream() : Stream.of();
        }

        @Override
        public Stream<CreatureEffectSource> getInternalTargetEffects() {
            return this.internalTargetEffects != null ? this.internalTargetEffects.stream() : Stream.of();
        }

        @Override
        public Stream<IExternalReference<ICreature>> getInternalTargetReferences() {
            return this.internalTargets != null ? this.internalTargets.stream().filter(ref -> ref != null)
                    .map(ref -> (IExternalReference<ICreature>) ref) : Stream.of();
        }

        @Override
        public CreatureFilterQuery getTargetingRestrictions() {
            return this.creatureFilter;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Usable [", "]");
            sj.add("kernel=" + this.kernel.toString());
            sj.add("timesUsed=" + Integer.toString(timesUsed));
            sj.add("totalNumberUsableTimes=" + Integer.toString(totalNumberUsableTimes));
            sj.add("selfOnly=" + Boolean.toString(selfOnly));
            if (this.useOnCreatureEffects != null) {
                sj.add("useOnCreatureEffects=" + useOnCreatureEffects.toString());
            }
            if (this.creatureFilter != null) {
                sj.add("creatureFilter=" + this.creatureFilter.toString());
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
            return Objects.hash(kernel, useOnCreatureEffects, internalTargetEffects, internalTargets,
                    totalNumberUsableTimes, selfOnly, creatureFilter, timesUsed);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Usable))
                return false;
            Usable other = (Usable) obj;
            return Objects.equals(kernel, other.kernel)
                    && Objects.equals(useOnCreatureEffects, other.useOnCreatureEffects)
                    && Objects.equals(internalTargetEffects, other.internalTargetEffects)
                    && Objects.equals(internalTargets, other.internalTargets)
                    && totalNumberUsableTimes == other.totalNumberUsableTimes && selfOnly == other.selfOnly
                    && Objects.equals(creatureFilter, other.creatureFilter) && timesUsed == other.timesUsed;
        }

    }
}