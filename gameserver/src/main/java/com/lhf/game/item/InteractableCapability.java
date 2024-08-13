package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.CreatureContainer.CreatureFilterQuery;
import com.lhf.game.IExternalReference;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
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

public interface InteractableCapability extends ItemCapability {
    public boolean isInteractionRepeatable();

    public int getInteractCount();

    public InteractableCapability setInteractCount(int count);

    public InteractableCapability incrementInteractCount();

    public CreatureFilterQuery getInteractUserRestrictions();

    public List<RichOutput> getInteractDisplayPages();

    public default RichOutput getInteractDisplay() {
        List<RichOutput> pages = this.getInteractDisplayPages();
        if (pages == null || pages.size() == 0) {
            return null;
        }
        int size = pages.size();
        if (this.isInteractDisplayPaged()) {
            if (size < 0) {
                size *= -1;
            }
            return pages.get(this.getInteractCount() % size);
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

    public default boolean isInteractDisplayPaged() {
        return false;
    }

    public List<IExternalReference<Area>> getExternalAreaReferences();

    public default Area getInteractArea() {
        final List<IExternalReference<Area>> retrieved = this.getExternalAreaReferences();
        if (retrieved == null || retrieved.isEmpty()) {
            return null;
        }
        for (IExternalReference<Area> iExternalReference : retrieved) {
            if (iExternalReference != null) {
                return iExternalReference.getReference();
            }
        }
        return null;
    }

    public Set<RoomEffectSource> getAreaInteractEffects();

    public Set<CreatureEffectSource> getInteractorEffects();
    // TODO: describe changes to self on interaction, needs DC for traps?

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

    public static void broadcast(CommandContext ctx, InteractableCapability capability, GameEvent.Builder<?> builder) {
        if (builder == null) {
            return;
        }
        InteractableCapability.broadcast(ctx, capability, builder.Build());
    }

    public static void broadcast(CommandContext ctx, InteractableCapability capability, GameEvent event) {
        if (event == null) {
            return;
        }
        if (capability != null) {
            final Area interactArea = capability.getInteractArea();
            if (interactArea != null) {
                Area.eventAccepter.accept(interactArea, event);
                return;
            }
        }
        if (ctx == null) {
            return;
        }
        final ICreature creature = ctx.getCreature();
        if (creature != null) {
            ICreature.eventAccepter.accept(creature, event);
            return;
        }
        ctx.receive(event);
    }

    private static void updateGuards(InteractableCapability capability) {
        if (capability == null) {
            return;
        }
        final Area area = capability.getInteractArea();
        if (area == null) {
            return;
        }
        final Set<String> guards = capability.getGuardsNames();
        if (guards == null || guards.isEmpty()) {
            return;
        }
        for (Iterator<String> guardIterator = guards.iterator(); guardIterator.hasNext();) {
            final String guardName = guardIterator.next();
            if (guardName == null || guardName.isBlank()) {
                guardIterator.remove();
                continue;
            }
            if (!area.hasCreature(guardName)) {
                guardIterator.remove();
            }
        }
    }

    public boolean isDispenser();

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
            InteractableCapability.broadcast(ctx, capability,
                    eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
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
            InteractableCapability.broadcast(ctx, capability,
                    eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT).setDescription("It is locked.")
                            .setOutputCallback(rob -> {
                                if (rob != null) {
                                    rob.appendString("This item is locked.");
                                }
                            }));
            return false;
        }
        final CreatureFilterQuery restrictions = capability.getInteractUserRestrictions();
        if (restrictions != null && !restrictions.test(interactor)) {
            InteractableCapability.broadcast(ctx, capability,
                    eventBuilder.setBroacast().setSubType(InteractOutMessageType.CANNOT)
                            .setDescription("Something prevents you from using it.").setOutputCallback(rob -> {
                                if (rob != null) {
                                    rob.appendString("This item has limitations on who can use it.");
                                }
                            }));
            return false;
        }
        InteractableCapability.broadcast(ctx, capability,
                eventBuilder.setBroacast().setPerformed().setOutputCallback(rob -> {
                    if (rob != null) {
                        rob.appendRichOutput(capability.getInteractDisplay());
                    }
                }));
        final Set<RoomEffectSource> areaInteractEffects = capability.getAreaInteractEffects();
        final Area area = capability.getInteractArea();
        if (area != null && areaInteractEffects != null && !areaInteractEffects.isEmpty()) {
            for (final RoomEffectSource roomEffectSource : areaInteractEffects) {
                if (roomEffectSource == null) {
                    continue;
                }
                InteractableCapability.broadcast(ctx, capability,
                        area.applyEffect(new RoomEffect(roomEffectSource, interactor, myItem)));
            }
        }
        final Set<CreatureEffectSource> interactorEffects = capability.getInteractorEffects();
        if (interactorEffects != null && !interactorEffects.isEmpty()) {
            for (final CreatureEffectSource creatureEffectSource : interactorEffects) {
                if (creatureEffectSource == null) {
                    continue;
                }
                InteractableCapability.broadcast(ctx, capability,
                        interactor.applyEffect(new CreatureEffect(creatureEffectSource, interactor, myItem)));
            }
        }
        final ItemContainerCapability itemContainer = myItem.getItemContainerCapability();
        if (capability.isDispenser() && itemContainer != null && area != null) {
            final IItem removed = itemContainer.removeOne();
            area.addItem(removed);
        }
        final Area ctxArea = ctx.getArea();
        final List<IExternalReference<Area>> areas = capability.getExternalAreaReferences();
        if (areas != null && ctxArea != null && ctxArea.hasCreature(interactor)) {
            for (final IExternalReference<Area> reference : areas) {
                final Area referred = reference.getReference();
                if (referred == null) {
                    continue;
                }
                if (referred.equals(ctxArea)) {
                    continue;
                }
                if (ctxArea.removeCreature(interactor)) {
                    ctxArea.announce(
                            RoomExitedEvent.getBuilder().setLeaveTaker(interactor).setBecauseOf(myItem).Build());
                    ICreature.eventAccepter.accept(ctx.getCreature(),
                            TickEvent.getBuilder().setTickType(TickType.ROOM).Build());
                    referred.addCreature(interactor);
                    break;
                }
            }
        } else if (myItem.getCreatureContainerCapability() != null) {
            final CreatureContainerCapability ccc = myItem.getCreatureContainerCapability();
            if (ccc.hasCapacity()) {
                ccc.addCreature(interactor);
            }
        }
        capability.incrementInteractCount();
        return true;
    }

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
        if (this.getInteractCount() > 0) {
            seeEventBuilder.addExtraInfo("It seems to have been interacted with already. ");
        }
    }

    public static enum Delta implements ICapabilityDelta, Consumer<InteractableCapability> {
        RESET_COUNT {
            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    arg0.setInteractCount(0);
                }
            }
        },
        INCREMENT {
            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    arg0.incrementInteractCount();
                }
            }
        },
        DECREMENT {

            @Override
            public void accept(InteractableCapability arg0) {
                if (arg0 != null) {
                    int count = arg0.getInteractCount() - 1;
                    arg0.setInteractCount(Integer.max(count, 0));
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
        private final boolean interactionRepeatable;
        private int interactCount;
        private final CreatureFilterQuery interactUserRestrictions;
        private final List<RichOutput> interactDisplayPages;
        private final Set<RoomEffectSource> areaInteractEffects;
        private final Set<CreatureEffectSource> interactorEffects;
        private final Set<String> guardsNames;
        private final List<Area.AreaReference> interactAreas;
        private final boolean dispenser;

        public static class Builder {
            private boolean interactionRepeatable;
            private int interactCount = 0;
            private CreatureFilterQuery interactUserRestrictions;
            private List<RichOutputBuilder> interactDisplayPages;
            private Set<RoomEffectSource> areaInteractEffects;
            private Set<CreatureEffectSource> interactorEffects;
            private List<Area.AreaReference> interactAreas;
            private Set<String> guardsNames;
            private boolean dispenser;

            public Builder() {
                this.interactionRepeatable = false;
                this.interactUserRestrictions = null;
                this.interactDisplayPages = null;
                this.areaInteractEffects = null;
                this.interactorEffects = null;
                this.interactAreas = null;
                this.guardsNames = null;
                this.dispenser = false;
            }

            public Builder reset() {
                this.interactionRepeatable = false;
                this.interactUserRestrictions = null;
                this.interactDisplayPages = null;
                this.areaInteractEffects = null;
                this.interactorEffects = null;
                this.interactCount = 0;
                this.interactAreas = null;
                this.guardsNames = null;
                this.dispenser = false;
                return this;
            }

            public boolean isDispenser() {
                return dispenser;
            }

            public Builder setDispenser(boolean dispenser) {
                this.dispenser = dispenser;
                return this;
            }

            public boolean isInteractionRepeatable() {
                return interactionRepeatable;
            }

            public Builder setInteractionRepeatable(boolean interactionRepeatable) {
                this.interactionRepeatable = interactionRepeatable;
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
                return interactUserRestrictions;
            }

            public Builder setInteractUserRestrictions(CreatureFilterQuery interactUserRestrictions) {
                this.interactUserRestrictions = interactUserRestrictions;
                return this;
            }

            public Builder adjustInteractUserRestrictions(Consumer<CreatureFilterQuery> adjustor) {
                if (adjustor != null) {
                    if (this.interactUserRestrictions == null) {
                        this.interactUserRestrictions = new CreatureFilterQuery();
                    }
                    adjustor.accept(this.interactUserRestrictions);
                }
                return this;
            }

            public List<RichOutputBuilder> getInteractDisplayPages() {
                return interactDisplayPages != null ? Collections.unmodifiableList(interactDisplayPages) : List.of();
            }

            public Builder setInteractDisplayPages(List<RichOutputBuilder> interactDisplayPages) {
                this.interactDisplayPages = interactDisplayPages;
                return this;
            }

            public Builder addInteractDisplayPage(RichOutputBuilder page) {
                if (page != null) {
                    if (this.interactDisplayPages == null) {
                        this.interactDisplayPages = new ArrayList<>();
                    }
                    this.interactDisplayPages.add(page);
                }
                return this;
            }

            public Builder addInteractDisplayPage(String page) {
                if (page != null) {
                    if (this.interactDisplayPages == null) {
                        this.interactDisplayPages = new ArrayList<>();
                    }
                    RichOutputBuilder pageBuilder = new RichOutputBuilder().appendString(page);
                    this.interactDisplayPages.add(pageBuilder);
                }
                return this;
            }

            public Builder addInteractDisplayPage(Consumer<RichOutputBuilder> pageBuilder) {
                if (pageBuilder != null) {
                    if (this.interactDisplayPages == null) {
                        this.interactDisplayPages = new ArrayList<>();
                    }
                    RichOutputBuilder builder = new RichOutputBuilder();
                    this.interactDisplayPages.add(builder);
                    pageBuilder.accept(builder);
                }
                return this;
            }

            public RichOutputBuilder createOrGetPageBuilder(int index) {
                if (this.interactDisplayPages == null) {
                    this.interactDisplayPages = new ArrayList<>();
                }
                if (index < 0 || index >= this.interactDisplayPages.size()) {
                    RichOutputBuilder page = new RichOutputBuilder();
                    this.interactDisplayPages.add(page);
                    return page;
                }
                return this.interactDisplayPages.get(index);
            }

            public Builder createOrEditPageBuilder(int index, Consumer<RichOutputBuilder> pageEditor) {
                RichOutputBuilder pageBuilder = this.createOrGetPageBuilder(index);
                if (pageBuilder != null && pageEditor != null) {
                    pageEditor.accept(pageBuilder);
                }
                return this;
            }

            public Builder clearPages() {
                if (this.interactDisplayPages != null) {
                    this.interactDisplayPages.clear();
                }
                return this;
            }

            public int size() {
                if (this.interactDisplayPages != null) {
                    return this.interactDisplayPages.size();
                }
                return 0;
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

            public Set<CreatureEffectSource> getInteractorEffects() {
                return interactorEffects != null ? Collections.unmodifiableSet(interactorEffects) : Set.of();
            }

            public Builder setInteractorEffects(Set<CreatureEffectSource> interactorEffects) {
                this.interactorEffects = interactorEffects;
                return this;
            }

            public Builder addInteractorEffect(CreatureEffectSource interactorEffect) {
                if (interactorEffect != null) {
                    if (this.interactorEffects == null) {
                        this.interactorEffects = new LinkedHashSet<>();
                    }
                    this.interactorEffects.add(interactorEffect);
                }
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
                builder.append("Builder [interactionRepeatable=").append(interactionRepeatable)
                        .append(", interactCount=").append(interactCount).append(", interactUserRestrictions=")
                        .append(interactUserRestrictions).append(", interactDisplayPages=").append(interactDisplayPages)
                        .append(", areaInteractEffects=").append(areaInteractEffects).append(", interactorEffects=")
                        .append(interactorEffects).append(", interactAreas=").append(interactAreas)
                        .append(", guardsNames=").append(guardsNames).append(", dispenser=").append(dispenser)
                        .append("]");
                return builder.toString();
            }

        }

        protected Interactable() {
            this.interactionRepeatable = false;
            this.interactCount = 0;
            this.interactAreas = new ArrayList<>();
            this.interactUserRestrictions = null;
            this.interactDisplayPages = new ArrayList<>();
            this.areaInteractEffects = new LinkedHashSet<>();
            this.interactorEffects = new LinkedHashSet<>();
            this.guardsNames = new LinkedHashSet<>();
            this.dispenser = false;
        }

        protected Interactable(Builder builder) {
            if (builder == null) {
                this.interactionRepeatable = false;
                this.interactCount = 0;
                this.interactAreas = new ArrayList<>();
                this.interactUserRestrictions = null;
                this.interactDisplayPages = new ArrayList<>();
                this.areaInteractEffects = new LinkedHashSet<>();
                this.interactorEffects = new LinkedHashSet<>();
                this.guardsNames = new LinkedHashSet<>();
                this.dispenser = false;
            } else {
                this.interactionRepeatable = builder.isInteractionRepeatable();
                this.interactCount = builder.getInteractCount();
                this.interactAreas = builder.interactAreas != null ? List.copyOf(builder.interactAreas) : List.of();
                this.interactUserRestrictions = builder.getInteractUserRestrictions() != null
                        ? new CreatureFilterQuery(builder.interactUserRestrictions)
                        : null;
                this.interactDisplayPages = builder.getInteractDisplayPages().stream().filter(rab -> rab != null)
                        .map(rab -> rab.build()).toList();
                this.areaInteractEffects = builder.getAreaInteractEffects().stream().filter(ef -> ef != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                this.interactorEffects = builder.getInteractorEffects().stream().filter(ef -> ef != null)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                this.guardsNames = builder.guardsNames != null ? new LinkedHashSet<>(builder.guardsNames) : null;
                this.dispenser = builder.isDispenser();
            }
        }

        @Override
        public boolean isDispenser() {
            return this.dispenser;
        }

        @Override
        public boolean isInteractionRepeatable() {
            return this.interactionRepeatable;
        }

        @Override
        public synchronized int getInteractCount() {
            return this.interactCount;
        }

        @Override
        public synchronized InteractableCapability setInteractCount(int count) {
            this.interactCount = count;
            return this;
        }

        @Override
        public synchronized InteractableCapability incrementInteractCount() {
            this.interactCount++;
            return this;
        }

        @Override
        public CreatureFilterQuery getInteractUserRestrictions() {
            return this.interactUserRestrictions;
        }

        @Override
        public List<RichOutput> getInteractDisplayPages() {
            return Collections.unmodifiableList(this.interactDisplayPages);
        }

        @Override
        public Set<RoomEffectSource> getAreaInteractEffects() {
            return this.areaInteractEffects != null ? Collections.unmodifiableSet(this.areaInteractEffects) : Set.of();
        }

        @Override
        public Set<CreatureEffectSource> getInteractorEffects() {
            return this.interactorEffects != null ? Collections.unmodifiableSet(this.interactorEffects) : Set.of();
        }

        @Override
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
            return Objects.hash(interactionRepeatable, interactUserRestrictions, interactDisplayPages,
                    areaInteractEffects, interactorEffects, interactAreas, dispenser);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Interactable))
                return false;
            Interactable other = (Interactable) obj;
            return interactionRepeatable == other.interactionRepeatable
                    && Objects.equals(interactUserRestrictions, other.interactUserRestrictions)
                    && Objects.equals(interactDisplayPages, other.interactDisplayPages)
                    && Objects.equals(areaInteractEffects, other.areaInteractEffects)
                    && Objects.equals(interactorEffects, other.interactorEffects)
                    && Objects.equals(interactAreas, other.interactAreas) && dispenser == other.dispenser;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Interactable [", "]");
            sj.add("interactionRepeatable=" + Boolean.toString(interactionRepeatable));
            sj.add("interactCount=" + Integer.toString(interactCount));
            sj.add("dispenser=" + Boolean.toString(dispenser));
            if (interactUserRestrictions != null) {
                sj.add("interactUserRestrictions=" + interactUserRestrictions.toString());
            }
            if (interactDisplayPages != null) {
                sj.add("interactDisplayPages=" + interactDisplayPages.toString());
            }
            if (areaInteractEffects != null) {
                sj.add("areaInteractEffects=" + areaInteractEffects.toString());
            }
            if (interactorEffects != null) {
                sj.add("interactorEffects=" + interactorEffects.toString());
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
