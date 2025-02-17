package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.IExternalReference;
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.UsableCapability.Usable.UsableBuilder;
import com.lhf.game.map.Area;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.RoomExitedEvent;
import com.lhf.messages.events.ItemInteractionEvent.Builder;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.messages.events.SeeEvent.ABuilder;
import com.lhf.messages.events.TickEvent;

public interface InteractableCapability extends EffectorCapability<Area, RoomEffectSource> {
    @Override
    public InteractableCapability adjustUses(int delta);

    @Override
    public InteractableCapability setUses(int count);

    /**
     * Returns an editable set of Guards names If any guards are present in the
     * area, interaction is prevented
     * 
     * @return
     */
    public Set<String> getGuardsNames();

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    public default void broadcast(CommandContext ctx, GameEvent event) {
        if (event == null) {
            return;
        }
        if (ctx == null) {
            return;
        }
        final Stream<IExternalReference<Area>> stream = this.getInternalTargetReferences();
        if (stream != null) {
            final AtomicBoolean sent = new AtomicBoolean(false);
            stream.filter(ref -> ref != null).forEachOrdered(ref -> {
                final Area area = ref.getReference();
                if (area != null) {
                    Area.eventAccepter.accept(area, event);
                    sent.set(true);
                }
            });
            if (sent.get()) {
                return;
            }
        }
        EffectorCapability.super.broadcast(ctx, event);
    }

    private static void updateGuards(InteractableCapability capability) {
        if (capability == null) {
            return;
        }
        final Set<String> guards = capability.getGuardsNames();
        if (guards == null || guards.isEmpty()) {
            return;
        }

        final Stream<IExternalReference<Area>> stream = capability.getInternalTargetReferences();
        if (stream == null) {
            return;
        }
        stream.filter(ref -> ref != null && ref.getReference() != null).forEachOrdered(ref -> {
            final Area area = ref.getReference();
        });
        for (Iterator<String> guardIterator = guards.iterator(); guardIterator.hasNext();) {
            final String guardName = guardIterator.next();
            if (guardName == null || guardName.isBlank()) {
                guardIterator.remove();
                continue;
            }
            Stream<IExternalReference<Area>> guardStream = capability.getInternalTargetReferences();
            if (guardStream == null) {
                return;
            }
            if (guardStream.filter(ref -> ref != null && ref.getReference() != null)
                    .noneMatch(ref -> ref.getReference().hasCreature(guardName))) {
                guardIterator.remove();
            }
        }
    }

    public boolean isDispenser();

    @Override
    default void applyEffectsOnTarget(CommandContext ctx, IItem myItem, Area target) {
        if (ctx == null) {
            return;
        }
        if (target == null) {
            target = ctx.getArea();
        }
        if (target == null) {
            return;
        }
        final Stream<RoomEffectSource> targetEffects = this.getTargetEffects();
        if (targetEffects != null) {
            targetEffects.filter(source -> source != null).forEachOrdered(source -> {
                final RoomEffect effect = new RoomEffect(source, ctx.getCreature(), myItem);
                this.broadcast(ctx, target.applyEffect(effect));
            });
        }
    }

    @Override
    default void applyEffectsOnInternalTargets(CommandContext ctx, IItem myItem) {
        if (ctx == null) {
            return;
        }
        final Stream<IExternalReference<Area>> internalTargets = this.getInternalTargetReferences();
        if (internalTargets == null) {
            return;
        }
        final Stream<RoomEffectSource> internalTargetEffects = this.getInternalTargetEffects();
        if (internalTargetEffects == null) {
            return;
        }
        internalTargets.filter(ref -> ref != null).map(ref -> ref.getReference()).filter(area -> area != null)
                .forEachOrdered(area -> {
                    internalTargetEffects.filter(source -> source != null).forEachOrdered(source -> {
                        final RoomEffect effect = new RoomEffect(source, ctx.getCreature(), myItem);
                        this.broadcast(ctx, area.applyEffect(effect));
                    });
                });
    }

    public static boolean interactWithItem(CommandContext ctx, IItem myItem) {
        if (ctx == null) {
            return false;
        }
        final ICreature interactor = ctx.getCreature();
        Builder eventBuilder = ItemInteractionEvent.getBuilder().setInteractor(interactor);
        if (interactor == null) {
            ctx.receive(eventBuilder.setNotBroadcast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("Only Creatures can interact.").setOutputCallback(rob -> {
                        if (rob != null) {
                            rob.appendString("Only Creatures can interact with things.");
                        }
                    }));
            return false;
        }
        if (myItem == null) {
            ctx.receive(eventBuilder.setNotBroadcast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("Does this emtpy space do something?"));
            return false;
        }
        eventBuilder.setTaggable(myItem);
        final InteractableCapability capability = myItem.getInteractableCapability();
        if (capability == null) {
            ctx.receive(eventBuilder.setNotBroadcast().setSubType(InteractOutMessageType.NO_METHOD)
                    .setDescription("It does nothing"));
            return false;
        }
        InteractableCapability.updateGuards(capability);
        final Set<String> guards = capability.getGuardsNames();
        if (guards != null && !guards.contains(interactor.getName())) {
            capability.broadcast(ctx, eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("This item is guarded").setOutputCallback(rob -> {
                        if (rob != null) {
                            rob.appendString("This item is guarded.");
                            final StringJoiner sj = new StringJoiner(", ", " It is guarded by: ", ". ")
                                    .setEmptyValue("");
                            guards.stream().filter(name -> name != null).forEachOrdered(name -> sj.add(name));
                            rob.appendString(sj.toString());
                        }
                    }));
            return false;
        }
        final LockingCapability locking = myItem.getLockingCapability();
        if (locking != null && !locking.canAccess(interactor)) {
            capability.broadcast(ctx, eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("It is locked.").setOutputCallback(rob -> {
                        if (rob != null) {
                            rob.appendString("This item is locked.");
                        }
                    }));
            return false;
        }
        final Predicate<ICreature> restrictions = capability.getUserRestrictions();
        if (restrictions != null && !restrictions.test(interactor)) {
            capability.broadcast(ctx, eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("Something prevents you from using it.").setOutputCallback(rob -> {
                        if (rob != null) {
                            rob.appendString("This item has limitations on who can use it.");
                        }
                    }));
            return false;
        }
        capability.broadcast(ctx, eventBuilder.setBroacast().setPerformed().setOutputCallback(rob -> {
            if (rob != null) {
                rob.appendRichOutput(capability.getUseDisplay());
            }
        }));
        final Area ctxArea = ctx.getArea();
        final Predicate<Area> targetRestriction = capability.getTargetingRestrictions();
        if (ctxArea != null && !targetRestriction.test(ctxArea)) {
            capability.broadcast(ctx, eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("Something prevents you from using it.").setOutputCallback(rob -> {
                        if (rob != null) {
                            rob.appendString("This item has limitations on where it can be used.");
                        }
                    }));
            return false;
        }
        if (ctxArea != null) {
            final Stream<RoomEffectSource> areaInteractEffects = capability.getTargetEffects();
            if (areaInteractEffects != null) {
                areaInteractEffects.filter(source -> source != null).forEachOrdered(roomEffectSource -> {
                    capability.broadcast(ctx,
                            ctxArea.applyEffect(new RoomEffect(roomEffectSource, interactor, myItem)));
                });
            }
        }
        final Stream<IExternalReference<Area>> areas = capability.getInternalTargetReferences();
        if (areas != null) {
            areas.filter(ref -> ref != null && ref.getReference() != null).forEachOrdered(ref -> {
                final Area area = ref.getReference();

                final Stream<RoomEffectSource> areaInteractEffects = capability.getInternalTargetEffects();
                if (area != null && areaInteractEffects != null) {
                    areaInteractEffects.filter(source -> source != null).forEachOrdered(roomEffectSource -> {
                        capability.broadcast(ctx,
                                area.applyEffect(new RoomEffect(roomEffectSource, interactor, myItem)));
                    });

                }

            });
        }

        final Stream<CreatureEffectSource> interactorEffects = capability.getUserEffects();
        if (interactorEffects != null) {
            interactorEffects.filter(effect -> effect != null).forEachOrdered(creatureEffectSource -> {
                capability.broadcast(ctx,
                        interactor.applyEffect(new CreatureEffect(creatureEffectSource, interactor, myItem)));
            });
        }
        final ItemContainerCapability itemContainer = myItem.getItemContainerCapability();
        if (capability.isDispenser() && itemContainer != null) {
            if (ctxArea != null) {
                final IItem removed = itemContainer.removeOne();
                ctxArea.addItem(removed);
            } else {
                final Stream<IExternalReference<Area>> areaStream = capability.getInternalTargetReferences();
                if (areaStream != null) {
                    areaStream.filter(ref -> ref != null && ref.getReference() != null).findFirst()
                            .ifPresent(ref -> ref.getReference().addItem(itemContainer.removeOne()));
                }
            }
        }
        final Stream<IExternalReference<Area>> areaStream = capability.getInternalTargetReferences();
        if (capability.isCreatureTransporter() && ctxArea != null && areaStream != null) {
            // if (areas != null && ctxArea != null && ctxArea.hasCreature(interactor)) {
            areaStream.filter(ref -> ref != null).map(ref -> ref.getReference())
                    .filter(area -> area != null && !ctxArea.equals(area)).findFirst().ifPresent(referred -> {
                        if (ctxArea.removeCreature(interactor)) {
                            ctxArea.announce(RoomExitedEvent.getBuilder().setLeaveTaker(interactor).setBecauseOf(myItem)
                                    .Build());
                            ICreature.eventAccepter.accept(ctx.getCreature(),
                                    TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
                            referred.addCreature(interactor);
                            break;
                        }
                    });

        } else if (myItem.getCreatureContainerCapability() != null) {
            final CreatureContainerCapability ccc = myItem.getCreatureContainerCapability();
            if (ccc.hasCapacity()) {
                ccc.addCreature(interactor);
            }
        }
        capability.useOnce();
        return true;
    }

    public boolean isCreatureTransporter();

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.INTERACTABLE;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        if (seeEventBuilder == null) {
            return;
        }
        seeEventBuilder.addExtraInfo("This item is Interactable. ");
        if (this.getTimesUsed() > 0) {
            seeEventBuilder.addExtraInfo("It seems to have been interacted with already. ");
        }
    }

    @Override
    public default boolean isEquippingRequired() {
        return false;
    }

    public static enum Delta implements ICapabilityDelta, Consumer<InteractableCapability> {
        RESET_COUNT {
            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    arg0.setUses(0);
                }
            }
        },
        INCREMENT {
            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    arg0.useOnce();
                }
            }
        },
        DECREMENT {

            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    int count = arg0.getTimesUsed() - 1;
                    arg0.setUses(Integer.max(count, 0));
                }
            }

        },
        NOOP {

            @Override
            public void accept(InteractableCapability arg0) {
                // does nothing
            }

        };

        @Override
        public abstract void accept(InteractableCapability arg0);

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

        @Override
        public void buildOutput(RichOutputBuilder builder) {
            if (builder == null) {
                return;
            }
            builder.appendString("After application, the interactable capability is");
            switch (this) {
            case DECREMENT:
                builder.appendString("decremented.");
                break;
            case INCREMENT:
                builder.appendString("incremented.");
                break;
            case NOOP:
                builder.appendString("unchanged.");
                break;
            case RESET_COUNT:
                builder.appendString("reset.");
                break;
            default:
                builder.appendString("unchanged.");
                break;
            }
        }
    }

    public default void acceptDelta(Delta delta) {
        if (delta != null) {
            delta.accept(this);
        }
    }

    public static InteractableCapability generateInteractableCapability() {
        return new Interactable();
    }

    public static final class Interactable implements InteractableCapability {
        private final EffectorKernel kernel;
        private final int totalNumberUsableTimes;
        private int interactCount;
        private final Set<RoomEffectSource> areaInteractEffects;
        private final Set<String> guardsNames;
        private final List<Area.AreaReference> interactAreas;
        private final boolean dispenser; // TODO: make this actually dispense somehow

        public static class Builder {
            private EffectorKernel.EffectorKernelBuilder kernel;
            private int totalNumberUsableTimes = -1;
            private int interactCount = 0;
            private Set<RoomEffectSource> areaInteractEffects;
            private List<Area.AreaReference> interactAreas;
            private Set<String> guardsNames;
            private boolean dispenser;

            public Builder() {
                this.kernel = EffectorKernel.getBuilder().setEquippingRequired(false);
                this.totalNumberUsableTimes = -1;
                this.areaInteractEffects = null;
                this.interactAreas = null;
                this.guardsNames = null;
                this.dispenser = false;
            }

            public boolean isDispenser() {
                return dispenser;
            }

            public Builder setDispenser(boolean dispenser) {
                this.dispenser = dispenser;
                return this;
            }

            public int getTotalNumberUsableTimes() {
                return totalNumberUsableTimes;
            }

            public Builder setTotalNumberUsableTimes(int totalNumberUsableTimes) {
                this.totalNumberUsableTimes = totalNumberUsableTimes;
                return this;
            }

            public int getInteractCount() {
                return interactCount;
            }

            public Builder setInteractCount(int interactCount) {
                this.interactCount = interactCount;
                return this;
            }

            public CreatureFilterQuery getInteractUserRestrictions() {
                return this.kernel.getUserRestrictions();
            }

            public Builder setInteractUserRestrictions(CreatureFilterQuery interactUserRestrictions) {
                this.kernel.setUserRestrictions(interactUserRestrictions);
                return this;
            }

            public List<RichOutputBuilder> getInteractDisplayPages() {
                return this.kernel.getUseDisplayPages();
            }

            public Builder setInteractDisplayPages(List<RichOutputBuilder> interactDisplayPages) {
                this.kernel.setUseDisplayPages(interactDisplayPages);
                return this;
            }

            public Builder addInteractDisplayPage(RichOutputBuilder page) {
                this.kernel.addUseDisplayPages(page);
                return this;
            }

            public Builder addInteractDisplayPage(String page) {
                this.kernel.addUseDisplayPage(page);
                return this;
            }

            public Builder addUseDisplayPage(Consumer<RichOutputBuilder> pageBuilder) {
                if (pageBuilder != null) {
                    RichOutputBuilder builder = new RichOutputBuilder();
                    this.kernel.addUseDisplayPages(builder);
                    pageBuilder.accept(builder);
                }
                return this;
            }

            public RichOutputBuilder createOrGetPageBuilder(int index) {
                return this.kernel.createOrGetPage(index);
            }

            public Builder createOrEditPageBuilder(int index, Consumer<RichOutputBuilder> pageEditor) {
                this.kernel.createOrEditPage(index, pageEditor);
                return this;
            }

            public Set<RoomEffectSource> getAreaInteractEffects() {
                return areaInteractEffects != null ? Collections.unmodifiableSet(this.areaInteractEffects) : Set.of();
            }

            public Builder setAreaInteractEffects(Set<RoomEffectSource> areaInteractEffects) {
                this.areaInteractEffects = areaInteractEffects;
                return this;
            }

            public Builder addAreaInteractEffect(RoomEffectSource areaInteractEffect) {
                if (areaInteractEffect != null) {
                    if (this.areaInteractEffects == null) {
                        this.areaInteractEffects = new LinkedHashSet<>();
                    }
                    this.areaInteractEffects.add(areaInteractEffect);
                }
                return this;
            }

            public Set<CreatureEffectSource.Builder> getInteractorEffects() {
                return this.kernel.getUserEffects();
            }

            public Builder setInteractorEffects(Set<CreatureEffectSource.Builder> interactorEffects) {
                this.kernel.setUserEffects(interactorEffects);
                return this;
            }

            public Builder addInteractorEffect(CreatureEffectSource.Builder interactorEffect) {
                this.kernel.addUserEffects(interactorEffect);
                return this;
            }

            public Set<String> getGuardsNames() {
                return guardsNames;
            }

            public Builder addGuard(String name) {
                if (name != null && !name.isBlank()) {
                    if (this.guardsNames == null) {
                        this.guardsNames = new LinkedHashSet<>();
                    }
                    this.guardsNames.add(name);
                }
                return this;
            }

            public Builder addGuard(ICreature creature) {
                if (creature != null) {
                    if (this.guardsNames == null) {
                        this.guardsNames = new LinkedHashSet<>();
                    }
                    this.guardsNames.add(creature.getName());
                }
                return this;
            }

            public Builder setGuardsNames(Set<String> guardsNames) {
                this.guardsNames = guardsNames;
                return this;
            }

            public List<Area.AreaReference> getInteractAreas() {
                return interactAreas;
            }

            public Builder setInteractAreas(List<Area.AreaReference> interactAreas) {
                this.interactAreas = interactAreas;
                return this;
            }

            public Builder addInteractArea(Area.AreaReference reference) {
                if (reference != null) {
                    if (this.interactAreas == null) {
                        this.interactAreas = new ArrayList<>();
                    }
                    this.interactAreas.add(reference);
                }
                return this;
            }

            public Builder adjustInteractAreas(Consumer<List<Area.AreaReference>> adjustor) {
                if (adjustor != null) {
                    if (this.interactAreas == null) {
                        this.interactAreas = new ArrayList<>();
                    }
                    adjustor.accept(interactAreas);
                }
                return this;
            }

            public InteractableCapability build() {
                return new Interactable(this);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Builder [kernel=").append(kernel).append(", totalNumberUsableTimes=")
                        .append(totalNumberUsableTimes).append(", interactCount=").append(interactCount)
                        .append(", areaInteractEffects=").append(areaInteractEffects).append(", interactAreas=")
                        .append(interactAreas).append(", guardsNames=").append(guardsNames).append(", dispenser=")
                        .append(dispenser).append("]");
                return builder.toString();
            }

        }

        protected Interactable() {
            this.kernel = EffectorKernel.getBuilder().build();
            this.totalNumberUsableTimes = -1;
            this.interactCount = 0;
            this.interactAreas = new ArrayList<>();
            this.areaInteractEffects = new LinkedHashSet<>();
            this.guardsNames = new LinkedHashSet<>();
            this.dispenser = false;
        }

        protected Interactable(Builder builder) {
            if (builder == null) {
                this.kernel = EffectorKernel.getBuilder().build();
                this.totalNumberUsableTimes = -1;
                this.interactCount = 0;
                this.interactAreas = new ArrayList<>();
                this.areaInteractEffects = new LinkedHashSet<>();
                this.guardsNames = new LinkedHashSet<>();
                this.dispenser = false;
            } else {
                this.kernel = builder.kernel.build();
                this.totalNumberUsableTimes = builder.getTotalNumberUsableTimes();
                this.interactCount = builder.getInteractCount();
                this.interactAreas = builder.interactAreas != null ? List.copyOf(builder.interactAreas) : List.of();
                this.areaInteractEffects = builder.getAreaInteractEffects().stream().filter(ef -> ef != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                this.guardsNames = builder.guardsNames != null ? new LinkedHashSet<>(builder.guardsNames) : null;
                this.dispenser = builder.isDispenser();
            }
        }

        @Override
        public boolean isCreatureTransporter() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Stream<RoomEffectSource> getInternalTargetEffects() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stream<IExternalReference<Area>> getInternalTargetReferences() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stream<ItemEffectSource> getSelfEffects() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ItemFilterQuery getSelfRestrictions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stream<RoomEffectSource> getTargetEffects() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Predicate<Area> getTargetingRestrictions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<RichOutput> getUseDisplayPages() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public com.lhf.messages.events.ItemUsedEvent.Builder getUsedEventBuilder(CommandContext ctx, IItem myItem,
                Area target) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stream<CreatureEffectSource> getUserEffects() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CreatureFilterQuery getUserRestrictions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean useOnce() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isDispenser() {
            return this.dispenser;
        }

        @Override
        public int getTotalNumberUsableTimes() {
            return this.totalNumberUsableTimes;
        }

        public boolean isInteractionRepeatable() {
            return this.hasUsesRemaining();
        }

        public synchronized int getInteractCount() {
            return this.interactCount;
        }

        public synchronized int getTimesUsed() {
            return this.interactCount;
        }

        public synchronized InteractableCapability setUses(int count) {
            this.interactCount = count;
            return this;
        }

        public synchronized InteractableCapability adjustUses(int delta) {
            this.interactCount += delta;
            return this;
        }

        public synchronized InteractableCapability incrementInteractCount() {
            this.interactCount++;
            return this;
        }

        public CreatureFilterQuery getInteractUserRestrictions() {
            return this.kernel.getUserRestrictions();
        }

        public List<RichOutput> getInteractDisplayPages() {
            return this.kernel.getUseDisplayPages();
        }

        public Set<RoomEffectSource> getAreaInteractEffects() {
            return this.areaInteractEffects != null ? Collections.unmodifiableSet(this.areaInteractEffects) : Set.of();
        }

        public Set<CreatureEffectSource> getInteractorEffects() {
            return this.kernel.getUserEffects().collect(Collectors.toSet());
        }

        public List<IExternalReference<Area>> getExternalAreaReferences() {
            return this.interactAreas != null
                    ? this.interactAreas.stream().filter(ref -> ref != null).map(ref -> (IExternalReference<Area>) ref)
                            .toList()
                    : List.of();
        }

        @Override
        public Set<String> getGuardsNames() {
            return this.guardsNames;
        }

        @Override
        public int hashCode() {
            return Objects.hash(kernel, interactionRepeatable, interactCount, areaInteractEffects, guardsNames,
                    interactAreas, dispenser);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Interactable))
                return false;
            Interactable other = (Interactable) obj;
            return Objects.equals(kernel, other.kernel) && interactionRepeatable == other.interactionRepeatable
                    && interactCount == other.interactCount
                    && Objects.equals(areaInteractEffects, other.areaInteractEffects)
                    && Objects.equals(guardsNames, other.guardsNames)
                    && Objects.equals(interactAreas, other.interactAreas) && dispenser == other.dispenser;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Interactable [", "]");
            sj.add("interactionRepeatable=" + Boolean.toString(interactionRepeatable));
            sj.add("interactCount=" + Integer.toString(interactCount));
            sj.add("dispenser=" + Boolean.toString(dispenser));
            sj.add("kernel=" + this.kernel.toString());
            if (areaInteractEffects != null) {
                sj.add("areaInteractEffects=" + areaInteractEffects.toString());
            }
            if (guardsNames != null) {
                sj.add("guardsNames=" + guardsNames.toString());
            }
            if (interactAreas != null) {
                sj.add("interactAreas=" + interactAreas.toString());
            }
            return sj.toString();
        }

    }

}
