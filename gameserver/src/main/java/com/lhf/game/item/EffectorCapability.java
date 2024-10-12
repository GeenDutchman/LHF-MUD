package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.Examinable;
import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.IExternalReference;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessorHub;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemUsedEvent;

/**
 * This interface unifies all the items with the capability to affect some
 * target < T extends {@link com.lhf.Examinable} > with an Effect generated from
 * a < Source extends {@link com.lhf.game.EntityEffectSource} >.
 */
interface EffectorCapability<T extends Examinable, Source extends EntityEffectSource> extends ItemCapability {
    // TODO: flatten EntityEffect and EntityEffectSource

    /**
     * Returns the total number of times that this item can be used.
     * 
     * If the number is <= 0, then the item can be used indefinetly.
     * 
     * @return usable times
     */
    public int getTotalNumberUsableTimes();

    /**
     * Returns the number of times that this item has been used.
     * 
     * Note that this number can be manipulated by other methods.
     * 
     * @return
     */
    public int getTimesUsed();

    /**
     * Checks to see if this item has any uses remaining.
     * 
     * In the default implementation:
     * 
     * If the number as returned by {@link #getTotalNumberUsableTimes()} is <= 0,
     * then this will return true. Otherwise, this will return the result of
     * {@link #getTotalNumberUsableTimes()} > {@link #getTimesUsed()}.
     * 
     * @return true if this item has uses remaing, false otherwise.
     */
    public default boolean hasUsesRemaining() {
        final int usableTimes = this.getTotalNumberUsableTimes();
        return (usableTimes <= 0) || (usableTimes > this.getTimesUsed());
    }

    /**
     * Adjusts the number of times used (as in {@link #getTimesUsed()}) by `delta`.
     * Note that effects like int overflow can occur.
     * 
     * 
     * @param delta can be any int, postitive, or negative
     * @return fluid interface, returns this
     */
    public EffectorCapability<T, Source> adjustUses(int delta);

    /**
     * Sets the number of times that this item has ostensibly been used (as in
     * {@link #getTimesUsed()}) to `count`.
     * 
     * @return fluid interface, returns this
     */
    public EffectorCapability<T, Source> setUses(int count);

    /**
     * Uses the item once
     * 
     * @return true if it still can be used, false otherwise
     */
    public boolean useOnce();

    /**
     * Gets the list of display pages for perusal.
     * 
     * @see {@link com.lhf.RichOutput RichOutput}
     * @return display pages
     */
    public List<RichOutput> getUseDisplayPages();

    /**
     * When this item is used, this can produce a {@link com.lhf.RichOutput}
     * describing the use or some other output. The {@link com.lhf.RichOutput}
     * returned may be an aggregate based on the result of
     * {@link #isUseDisplayPaged()}. The source for the output is usually
     * {@link #getUseDisplayPages()}.
     * 
     * @return RichOutput
     */
    public default RichOutput getUseDisplay() {
        List<RichOutput> pages = this.getUseDisplayPages();
        if (pages == null || pages.size() == 0) {
            return null;
        }
        int size = pages.size();
        if (this.isUseDisplayPaged()) {
            if (size < 0) {
                size *= -1;
            }
            return pages.get(this.getTimesUsed() % size);
        }
        if (size == 1) {
            return pages.get(0);
        }
        RichOutputBuilder builder = new RichOutputBuilder();
        for (RichOutput richOutput : pages) {
            if (richOutput == null) {
                continue;
            }
            builder.appendRichOutput(richOutput);
        }
        return builder.build();
    }

    /**
     * Determines if, when the item is used, all of the {@link com.lhf.RichOutput}
     * returned by {@link #getUseDisplayPages()} are used, or just one list element
     * per use.
     * 
     * @return true if so, false otherwise
     */
    public default boolean isUseDisplayPaged() {
        return false;
    }

    @Override
    default boolean isStateful() {
        return true;
    }

    /**
     * Returns an event builder for the use of this item's capability based on the
     * provided, context, item, and target.
     * 
     * @param ctx    {@link com.lhf.messages.CommandContext CommandContext}
     * @param myItem {@link com.lhf.game.item.IItem item}
     * @param target {@link T}
     * @return {@link com.lhf.messages.events.ItemUsedEvent.Builder}
     */
    public ItemUsedEvent.Builder getUsedEventBuilder(CommandContext ctx, IItem myItem, T target);

    /**
     * Broadcasts the provided event according to the provided context. If there is
     * a Battle going on, the event maybe contained to the Battle. Otherwise it is
     * announced to the local Area, and as a last resort, it is only sent to the
     * user of the item.
     * 
     * @param ctx
     * @param event
     */
    public default void broadcast(CommandContext ctx, GameEvent.Builder<?> builder) {
        if (builder == null) {
            return;
        }
        this.broadcast(ctx, builder.Build());
    }

    /**
     * Broadcasts the provided event according to the provided context. If there is
     * a Battle going on, the event maybe contained to the Battle. Otherwise it is
     * announced to the local Area, and as a last resort, it is only sent to the
     * user of the item.
     * 
     * @param ctx
     * @param event
     */
    public default void broadcast(CommandContext ctx, GameEvent event) {
        if (event == null) {
            return;
        }
        if (ctx == null) {
            return;
        }
        GameEventProcessorHub hub = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (hub == null) {
            hub = ctx.getArea();
        }
        if (hub != null) {
            hub.announce(event);
            return;
        }
        final ICreature creature = ctx.getCreature();
        if (creature != null) {
            ICreature.eventAccepter.accept(creature, event);
            return;
        }
        ctx.receive(event);
    }

    /**
     * <p>
     * Checks that the User (as provided by the context) of the item meets the
     * requirements to successfully use this item's capability.
     * </p>
     * <p>
     * It may send an event either to the User or
     * {@link #broadcast(CommandContext, com.lhf.messages.events.GameEvent.Builder)
     * broadcast} it.
     * </p>
     * Note that this my have the side effect of removing a key from the user if the
     * item is locked and needs such a key.
     * 
     * @param ctx
     * @param myItem
     * @param target
     * @return true if the user meets the requirements or false otherwise
     */
    public default boolean checkUser(CommandContext ctx, IItem myItem, T target) {
        if (ctx == null) {
            return false;
        }
        final ICreature user = ctx.getCreature();
        if (user == null) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target).setItemUser(user);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("Only Creatures can utilize items."));
            }
            return false;
        }
        final Predicate<ICreature> userRestrictions = this.getUserRestrictions();
        if (userRestrictions != null && !userRestrictions.test(user)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target).setItemUser(user);
            if (eventBuilder != null) {
                this.broadcast(ctx, eventBuilder.setBroacast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("This item has limitations on who can use it."));
            }
            return false;
        }
        final LockingCapability locking = myItem.getLockingCapability();
        if (locking != null && !locking.canAccess(user)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                this.broadcast(ctx, eventBuilder.setBroacast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("It is locked."));
            }
            return false;
        }
        return true;
    }

    /**
     * Checks that this item meets certain criteria before it is used, among which
     * if it {@link #hasUsesRemaining()} and {@link #getSelfRestrictions()}.
     * 
     * @param ctx
     * @param myItem
     * @param target
     * @return true if requirements are met, false otherwise
     */
    public default boolean checkSelfRestrictions(CommandContext ctx, IItem myItem, T target) {
        if (ctx == null) {
            return false;
        }
        if (myItem == null) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("This item cannot be used right now."));
            }
            return false;
        }
        if (!this.hasUsesRemaining()) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.USED_UP));
            }
            return false;
        }
        final ItemFilterQuery restrictions = this.getSelfRestrictions();
        if (restrictions != null && !restrictions.test(myItem)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("This item cannot be used right now, it has requirements that are not met."));
            }
            return false;
        }

        return true;
    }

    /**
     * This checks if the target is targetable by this item. This mostly utilizes
     * {@link #getTargetingRestrictions()}.
     * 
     * @param ctx
     * @param myItem
     * @param target
     * @return true if the target can indeed be targeted, false otherwise
     */
    public default boolean checkTarget(CommandContext ctx, IItem myItem, T target) {
        if (ctx == null) {
            return false;
        }
        if (target == null) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.NO_USES)
                        .addMessage("You must target something."));
            }
            return false;
        }
        final Predicate<T> restrictions = this.getTargetingRestrictions();
        if (restrictions != null && !restrictions.test(target)) {
            ItemUsedEvent.Builder eventBuilder = this.getUsedEventBuilder(ctx, myItem, target);
            if (eventBuilder != null) {
                ctx.receive(eventBuilder.setNotBroadcast().setSubType(ItemUsedEvent.UseOutMessageOption.CANNOT)
                        .addMessage("The target does not fit the restrictions of this item."));
            }
            return false;
        }
        return true;
    }

    /**
     * Applies the effects from {@link #getTargetEffects()} upon the `target`.
     * 
     * @param ctx
     * @param myItem
     * @param target
     */
    public void applyEffectsOnTarget(CommandContext ctx, IItem myItem, T target);

    /**
     * If this capability has any listed internal targets according to
     * {@link #getInternalTargetReferences()}, then this will apply any effects
     * provided by {@link #getInternalTargetEffects()}.
     * 
     * @param ctx
     * @param myItem
     */
    public void applyEffectsOnInternalTargets(CommandContext ctx, IItem myItem);

    /**
     * If any effects are listed by {@link #getUserEffects()}, then they will be
     * applied to the user (as supplied by the context) upon use.
     * 
     * @param ctx
     * @param myItem
     */
    public default void applyEffectsOnUser(CommandContext ctx, IItem myItem) {
        if (ctx == null) {
            return;
        }
        final ICreature user = ctx.getCreature();
        if (user == null) {
            return;
        }
        final Stream<CreatureEffectSource> userEffects = this.getUserEffects();
        if (userEffects != null) {
            userEffects.filter(source -> source != null).forEachOrdered(source -> {
                final CreatureEffect effect = new CreatureEffect(source, user, myItem);
                this.broadcast(ctx, user.applyEffect(effect));
            });
        }
    }

    /**
     * If any effects are listed by {@link #getSelfEffects()}, then they will be
     * applied to this item `myItem` upon use.
     * 
     * @param ctx
     * @param myItem
     */
    public default void applyEffectsOnItem(CommandContext ctx, IItem myItem) {
        if (ctx == null || myItem == null) {
            return;
        }
        final Stream<ItemEffectSource> selfEffects = this.getSelfEffects();
        if (selfEffects != null) {
            selfEffects.filter(source -> source != null).forEachOrdered(source -> {
                final ItemEffect effect = new ItemEffect(source, ctx.getCreature(), myItem);
                this.broadcast(ctx, myItem.applyEffect(effect));
            });
        }
    }

    /**
     * This is a hook function for implementing classes to perform any final
     * preliminary checks before the item is used.
     * 
     * Defaults to `true`.
     * 
     * @param ctx
     * @param myItem
     * @param target
     * @return true if the item can be used, false otherwise
     */
    public default boolean customChecks(CommandContext ctx, IItem myItem, T target) {
        return true;
    }

    /**
     * Upon use, this applies any effects listed by {@link #getTargetEffects()} upon
     * the target.
     * 
     * @param ctx
     * @param myItem
     * @param target
     * @return
     */
    public default boolean useItemOnTarget(CommandContext ctx, IItem myItem, T target) {
        if (ctx == null) {
            return false;
        }
        if (!this.checkSelfRestrictions(ctx, myItem, target)) {
            return false;
        }
        if (!this.checkUser(ctx, myItem, target)) {
            return false;
        }
        if (!this.checkTarget(ctx, myItem, target)) {
            return false;
        }

        if (!this.customChecks(ctx, myItem, target)) {
            return false;
        }

        this.applyEffectsOnTarget(ctx, myItem, target);
        this.applyEffectsOnInternalTargets(ctx, myItem);
        this.applyEffectsOnUser(ctx, myItem);
        this.applyEffectsOnItem(ctx, myItem);
        return true;
    }

    /**
     * This returns an {@link com.lhf.game.ItemContainer.ItemFilterQuery
     * ItemFilterQuery} to use as an Predicate<IItem> that must test true before the
     * item can be used.
     * 
     * @return {@link com.lhf.game.ItemContainer.ItemFilterQuery ItemFilterQuery} or
     *         null
     */
    public ItemFilterQuery getSelfRestrictions();

    /**
     * This returns an {@link com.lhf.game.CreatureContainer.CreatureFilterQuery
     * CreatureFilterQuery} to use as an Predicate<Icreature> that must test true
     * before the item can be used.
     * 
     * @return {@link com.lhf.game.CreatureContainer.CreatureFilterQuery
     *         CreatureFilterQuery} or null
     */
    public CreatureFilterQuery getUserRestrictions();

    /**
     * Returns a Stream of {@link com.lhf.game.item.ItemEffectSource} that will
     * apply to this item.
     * 
     * @return Stream or null
     */
    public Stream<ItemEffectSource> getSelfEffects();

    /**
     * Returns a Stream of {@link com.lhf.game.creature.CreatureEffectSource} that
     * will apply to the user of the item.
     * 
     * @return Stream or null
     */
    public Stream<CreatureEffectSource> getUserEffects();

    /**
     * Returns a stream of {@link Source EffectSource}s that will affect the target
     * on use.
     * 
     * @return Stream or null
     */
    public Stream<Source> getTargetEffects();

    /**
     * Returns a stream of {@link Source EffectSource}s that will affect the
     * internal targets on use.
     * 
     * @return Stream or null
     */
    public Stream<Source> getInternalTargetEffects();

    /**
     * This returns a {@link java.util.function.Predicate}< {@link T} > that must
     * test true for the target to be targeted.
     * 
     * @return Predicate
     */
    public Predicate<T> getTargetingRestrictions();

    /**
     * Returns a Stream of {@link com.lhf.game.IExternalReference}< {@link T} > that
     * this capability tracks.
     * 
     * @return Stream or null
     */
    public Stream<IExternalReference<T>> getInternalTargetReferences();

    public static final class EffectorKernel {
        private final ItemFilterQuery selfRestrictions;
        private final CreatureFilterQuery userRestrictions;
        private final Set<ItemEffectSource> selfEffects;
        private final Set<CreatureEffectSource> userEffects;
        private final List<RichOutput> useDisplayPages;
        private final boolean useDisplayPaged;

        public static final class EffectorKernelBuilder {
            public ItemFilterQuery selfRestrictions;
            public CreatureFilterQuery userRestrictions;
            public Set<ItemEffectSource.Builder> selfEffects;
            public Set<CreatureEffectSource.Builder> userEffects;
            public List<RichOutput.RichOutputBuilder> useDisplayPages;
            public boolean useDisplayPaged;

            public ItemFilterQuery getSelfRestrictions() {
                return selfRestrictions;
            }

            public EffectorKernelBuilder setSelfRestrictions(ItemFilterQuery selfRestrictions) {
                this.selfRestrictions = selfRestrictions;
                return this;
            }

            public CreatureFilterQuery getUserRestrictions() {
                return userRestrictions;
            }

            public EffectorKernelBuilder setUserRestrictions(CreatureFilterQuery userRestrictions) {
                this.userRestrictions = userRestrictions;
                return this;
            }

            public Set<ItemEffectSource.Builder> getSelfEffects() {
                return selfEffects;
            }

            public EffectorKernelBuilder setSelfEffects(Set<ItemEffectSource.Builder> selfEffects) {
                this.selfEffects = selfEffects;
                return this;
            }

            public EffectorKernelBuilder addSelfEffects(ItemEffectSource.Builder... sources) {
                if (sources != null) {
                    if (this.selfEffects == null) {
                        this.selfEffects = new LinkedHashSet<>();
                    }
                    for (ItemEffectSource.Builder source : sources) {
                        if (source == null) {
                            continue;
                        }
                        this.selfEffects.add(source);
                    }
                }
                return this;
            }

            public EffectorKernelBuilder addSelfEffects(Collection<ItemEffectSource.Builder> sources) {
                if (sources != null) {
                    if (this.selfEffects == null) {
                        this.selfEffects = new LinkedHashSet<>();
                    }
                    for (ItemEffectSource.Builder source : sources) {
                        if (source == null) {
                            continue;
                        }
                        this.selfEffects.add(source);
                    }
                }
                return this;
            }

            public Set<CreatureEffectSource.Builder> getUserEffects() {
                return userEffects;
            }

            public EffectorKernelBuilder setUserEffects(Set<CreatureEffectSource.Builder> userEffects) {
                this.userEffects = userEffects;
                return this;
            }

            public EffectorKernelBuilder addUserEffects(CreatureEffectSource.Builder... sources) {
                if (sources != null) {
                    if (this.userEffects == null) {
                        this.userEffects = new LinkedHashSet<>();
                    }
                    for (CreatureEffectSource.Builder source : sources) {
                        if (source == null) {
                            continue;
                        }
                        this.userEffects.add(source);
                    }
                }
                return this;
            }

            public EffectorKernelBuilder addUserEffects(Collection<CreatureEffectSource.Builder> sources) {
                if (sources != null) {
                    if (this.userEffects == null) {
                        this.userEffects = new LinkedHashSet<>();
                    }
                    for (CreatureEffectSource.Builder source : sources) {
                        if (source == null) {
                            continue;
                        }
                        this.userEffects.add(source);
                    }
                }
                return this;
            }

            public List<RichOutput.RichOutputBuilder> getUseDisplayPages() {
                return useDisplayPages;
            }

            public EffectorKernelBuilder setUseDisplayPages(List<RichOutput.RichOutputBuilder> useDisplayPages) {
                this.useDisplayPages = useDisplayPages;
                return this;
            }

            public EffectorKernelBuilder addUseDisplayPages(RichOutput.RichOutputBuilder... builders) {
                if (builders != null) {
                    if (this.useDisplayPages == null) {
                        this.useDisplayPages = new ArrayList<>();
                    }
                    for (RichOutput.RichOutputBuilder builder : builders) {
                        if (builder == null) {
                            continue;
                        }
                        this.useDisplayPages.add(builder);
                    }
                }
                return this;
            }

            public EffectorKernelBuilder addUseDisplayPages(Collection<RichOutput.RichOutputBuilder> builders) {
                if (builders != null) {
                    if (this.useDisplayPages == null) {
                        this.useDisplayPages = new ArrayList<>();
                    }
                    for (RichOutput.RichOutputBuilder builder : builders) {
                        if (builder == null) {
                            continue;
                        }
                        this.useDisplayPages.add(builder);
                    }
                }
                return this;
            }

            public boolean isUseDisplayPaged() {
                return useDisplayPaged;
            }

            public EffectorKernelBuilder setUseDisplayPaged(boolean useDisplayPaged) {
                this.useDisplayPaged = useDisplayPaged;
                return this;
            }

            public EffectorKernel build() {
                return new EffectorKernel(this);
            }

        }

        public static EffectorKernelBuilder getBuilder() {
            return new EffectorKernelBuilder();
        }

        protected final static <T> Set<T> defensiveSetCopy(Set<T> toCopy) {
            return toCopy != null ? Collections.unmodifiableSet(new LinkedHashSet<>(toCopy)) : null;
        }

        private EffectorKernel(EffectorKernelBuilder builder) {
            this.selfRestrictions = builder.getSelfRestrictions();
            this.userRestrictions = builder.getUserRestrictions();
            this.selfEffects = builder.selfEffects == null ? null
                    : builder.selfEffects.stream().filter(effect -> effect != null).map(effect -> effect.build())
                            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                    Collections::unmodifiableSet));
            this.userEffects = builder.userEffects == null ? null
                    : builder.userEffects.stream().filter(effect -> effect != null).map(effect -> effect.build())
                            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                    Collections::unmodifiableSet));
            this.useDisplayPages = builder.useDisplayPages == null ? null
                    : builder.useDisplayPages.stream().filter(effect -> effect != null).map(effect -> effect.build())
                            .collect(Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new),
                                    Collections::unmodifiableList));
            this.useDisplayPaged = builder.isUseDisplayPaged();
        }

        public EffectorKernel(ItemFilterQuery selfRestrictions, CreatureFilterQuery userRestrictions,
                Set<ItemEffectSource> selfEffects, Set<CreatureEffectSource> userEffects,
                List<RichOutput> useDisplayPages, boolean useDisplayPaged) {
            this.selfRestrictions = selfRestrictions;
            this.userRestrictions = userRestrictions;
            this.selfEffects = EffectorKernel.defensiveSetCopy(selfEffects);
            this.userEffects = EffectorKernel.defensiveSetCopy(userEffects);
            this.useDisplayPages = useDisplayPages == null ? null
                    : Collections.unmodifiableList(new ArrayList<>(useDisplayPages));
            this.useDisplayPaged = useDisplayPaged;
        }

        public final ItemFilterQuery getSelfRestrictions() {
            return selfRestrictions;
        }

        public final CreatureFilterQuery getUserRestrictions() {
            return userRestrictions;
        }

        public final Stream<ItemEffectSource> getSelfEffects() {
            return selfEffects != null ? selfEffects.stream() : Stream.of();
        }

        public final Stream<CreatureEffectSource> getUserEffects() {
            return userEffects != null ? userEffects.stream() : Stream.of();
        }

        public final boolean isUseDisplayPaged() {
            return this.useDisplayPaged;
        }

        public List<RichOutput> getUseDisplayPages() {
            return useDisplayPages;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("EffectorKernel [selfRestrictions=").append(selfRestrictions).append(", userRestrictions=")
                    .append(userRestrictions).append(", selfEffects=").append(selfEffects).append(", userEffects=")
                    .append(userEffects).append(", useDisplayPages=").append(useDisplayPages)
                    .append(", useDisplayPaged=").append(useDisplayPaged).append("]");
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(selfRestrictions, userRestrictions, selfEffects, userEffects, useDisplayPages,
                    useDisplayPaged);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof EffectorKernel))
                return false;
            EffectorKernel other = (EffectorKernel) obj;
            return Objects.equals(selfRestrictions, other.selfRestrictions)
                    && Objects.equals(userRestrictions, other.userRestrictions)
                    && Objects.equals(selfEffects, other.selfEffects) && Objects.equals(userEffects, other.userEffects)
                    && Objects.equals(useDisplayPages, other.useDisplayPages)
                    && useDisplayPaged == other.useDisplayPaged;
        }

    }

}
