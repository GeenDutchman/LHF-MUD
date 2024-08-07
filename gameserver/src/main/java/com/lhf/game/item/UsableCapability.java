package com.lhf.game.item;

import java.util.ArrayList;
import java.util.Collections;
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
import com.lhf.game.ItemContainer.ItemFilterQuery;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.map.Area;
import com.lhf.game.map.RoomEffect;
import com.lhf.game.map.RoomEffectSource;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessorHub;
import com.lhf.messages.events.BattleRoundEvent;
import com.lhf.messages.events.BattleRoundEvent.RoundAcceptance;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemUsedEvent;
import com.lhf.messages.events.ItemUsedEvent.UseOutMessageOption;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface UsableCapability extends ItemCapability {
    public int getTotalNumberUsableTimes();

    public int getTimesUsed();

    public default boolean hasUsesRemaining() {
        int usableTimes = this.getTotalNumberUsableTimes();
        return (usableTimes <= 0) || (usableTimes > this.getTimesUsed());
    }

    public boolean requiresEquipping();

    public boolean isSelfOnly();

    public UsableCapability adjustUses(int uses);

    /**
     * Uses the item once
     * 
     * @return true if it still can be used, false otherwise
     */
    public boolean useOnce();

    public CreatureFilterQuery getUserRestrictions();

    public CreatureFilterQuery affectsCreaturesLike();

    public ItemFilterQuery affectsItemsLike();

    // how to search Areas?? with concrete attributes?
    public Set<CreatureEffectSource> getUseOnCreatureEffects();

    // TODO: concrete way to describe effects on items?
    public Set<RoomEffectSource> getUseOnAreaEffects();

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.USABLE;
    }

    @Override
    public default boolean isStateful() {
        return true;
    }

    public List<RichOutput> getUseDisplayPages();

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

    public default boolean isUseDisplayPaged() {
        return false;
    }

    private ItemUsedEvent.Builder getCreatureUseBuilder(CommandContext ctx, IItem myItem, ICreature target) {
        return ItemUsedEvent.getBuilder().setUsable(myItem).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature())
                .addMessage(this.getUseOnCreatureEffects() == null || this.getUseOnAreaEffects().isEmpty() ? null
                        : "Affects try to take hold.")
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

    private ItemUsedEvent.Builder getItemUseBuilder(CommandContext ctx, IItem myItem, IItem target) {
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

    private ItemUsedEvent.Builder getAreaUseBuilder(CommandContext ctx, IItem myItem, Area target) {
        return ItemUsedEvent.getBuilder().setUsable(myItem).setSubType(UseOutMessageOption.OK)
                .setItemUser(ctx.getCreature())
                .addMessage(this.getUseOnAreaEffects() == null || this.getUseOnAreaEffects().isEmpty() ? null
                        : "Affects try to take hold.")
                .editMessage(builder -> {
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

    public static boolean useItem(CommandContext ctx, IItem myItem) {
        return UsableCapability.useOn(ctx, myItem, ctx.getCreature());
    }

    public static boolean useOn(CommandContext ctx, IItem myItem, ICreature creature) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                .setUsable(myItem);
        if (creature == null || myItem == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .addMessage("Must target a creature with an item!").Build());
            return false;
        }
        UsableCapability capability = myItem.getUsableCapability();
        if (capability == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).addMessage("That item is not usable!")
                    .Build());
            return false;
        }
        if (!capability.hasUsesRemaining()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        final ICreature myUser = ctx.getCreature();
        if (capability.requiresEquipping() && !myUser.getEquipmentSlots().containsValue(myItem)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).Build());
            return false;
        }
        final CreatureFilterQuery userRestriction = capability.getUserRestrictions();
        if (userRestriction != null && !userRestriction.test(myUser)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this for some reason.").Build());
            return false;
        }
        if (capability.isSelfOnly() && !creature.equals(myUser)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You can only use this on yourself!").Build());
            return false;
        }
        final CreatureFilterQuery query = capability.affectsCreaturesLike();
        if (query != null && !query.test(creature)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("The target does not meet the requirements!").Build());
            return false;
        }

        if (ctx.getSubAreaForSort(SubAreaSort.BATTLE) != null) {
            SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            if (bm.hasCreature(creature) && !bm.hasCreature(ctx.getCreature())) {
                // give out of turn message
                bm.addCreature(ctx.getCreature());
                ctx.receive(BattleRoundEvent.getBuilder().setNeedSubmission(RoundAcceptance.REJECTED).setNotBroadcast()
                        .Build());
                return false;
            }
        }
        UsableCapability.sendNotice(ctx, creature, capability.getCreatureUseBuilder(ctx, myItem, creature));
        UsableCapability.applyCreatureEffects(ctx, myItem, creature);
        return true;
    }

    public static boolean useOn(CommandContext ctx, IItem myItem, IItem targetedItem) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                .setUsable(myItem);
        if (targetedItem == null || myItem == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        UsableCapability capability = myItem.getUsableCapability();
        if (capability == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).addMessage("That item is not usable!")
                    .Build());
            return false;
        }
        if (!capability.hasUsesRemaining()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        final ICreature myUser = ctx.getCreature();
        if (capability.requiresEquipping() && !myUser.getEquipmentSlots().containsValue(myItem)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).Build());
            return false;
        }
        final CreatureFilterQuery userRestriction = capability.getUserRestrictions();
        if (userRestriction != null && !userRestriction.test(myUser)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this for some reason.").Build());
            return false;
        }
        UsableCapability.sendNotice(ctx, myUser, capability.getItemUseBuilder(ctx, myItem, targetedItem));

        // TODO: some way to declare what I'm doing to the item

        // TODO: dealing with keys and lockables
        // final Consumer<IItem> visitor = capability.produceItemConsumer(ctx);
        // if (visitor == null) {
        // ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
        // return false;
        // }
        // visitor.accept(targetedItem);
        return true;
    }

    public static boolean useOn(CommandContext ctx, IItem myItem, Area area) {
        ItemUsedEvent.Builder useOutMessage = ItemUsedEvent.getBuilder().setItemUser(ctx.getCreature())
                .setUsable(myItem);
        if (area == null || myItem == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).Build());
            return false;
        }
        UsableCapability capability = myItem.getUsableCapability();
        if (capability == null) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES).addMessage("That item is not usable!")
                    .Build());
            return false;
        }
        if (!capability.hasUsesRemaining()) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.USED_UP).Build());
            return false;
        }
        final ICreature myUser = ctx.getCreature();
        if (capability.requiresEquipping() && !myUser.getEquipmentSlots().containsValue(myItem)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.REQUIRE_EQUIPPED).Build());
            return false;
        }
        final CreatureFilterQuery userRestriction = capability.getUserRestrictions();
        if (userRestriction != null && !userRestriction.test(myUser)) {
            ctx.receive(useOutMessage.setSubType(UseOutMessageOption.NO_USES)
                    .setMessage("You cannot use this for some reason.").Build());
            return false;
        }
        UsableCapability.sendNotice(ctx, ctx.getCreature(), capability.getAreaUseBuilder(ctx, myItem, area));
        UsableCapability.applyAreaEffects(ctx, myItem, area);
        return true;
    }

    private static void sendNotice(CommandContext ctx, ICreature creature, GameEvent event) {
        if (creature == null || event == null) {
            return;
        }
        GameEventProcessorHub hub = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (hub == null) {
            hub = ctx.getArea();
        }
        if (hub != null) {
            hub.announce(event);
        } else {
            ctx.receive(event);
            if (!creature.equals(ctx.getCreature())) {
                ICreature.eventAccepter.accept(creature, event);
            }
        }
    }

    private static void sendNotice(CommandContext ctx, ICreature creature, GameEvent.Builder<?> eventBuilder) {
        if (creature == null || eventBuilder == null) {
            return;
        }
        GameEventProcessorHub hub = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (hub == null) {
            hub = ctx.getArea();
        }
        if (hub != null) {
            hub.announce(eventBuilder.setBroacast().Build());
        } else {
            ctx.receive(eventBuilder.setNotBroadcast());
            if (!creature.equals(ctx.getCreature())) {
                ICreature.eventAccepter.accept(creature, eventBuilder.setBroacast().Build());
            }
        }
    }

    private static void applyCreatureEffects(CommandContext ctx, IItem item, ICreature creature) {
        if (creature == null || item == null) {
            return;
        }
        UsableCapability capability = item.getUsableCapability();
        if (capability == null) {
            return;
        }
        final Set<CreatureEffectSource> effects = capability.getUseOnCreatureEffects();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (final CreatureEffectSource source : effects) {
            final CreatureEffect effect = new CreatureEffect(source, ctx.getCreature(), item);
            UsableCapability.sendNotice(ctx, creature, creature.applyEffect(effect));
        }
    }

    private static void applyAreaEffects(CommandContext ctx, IItem item, Area area) {
        if (area == null || item == null) {
            return;
        }
        UsableCapability capability = item.getUsableCapability();
        if (capability == null) {
            return;
        }
        final Set<RoomEffectSource> effects = capability.getUseOnAreaEffects();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (final RoomEffectSource source : effects) {
            final RoomEffect effect = new RoomEffect(source, ctx.getCreature(), item);
            UsableCapability.sendNotice(ctx, ctx.getCreature(), area.applyEffect(effect));
        }
    }

    public static UsableCapability generateUsableCapability() {
        return new Usable();
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

    public static final class Usable implements UsableCapability {
        private final Set<CreatureEffectSource> useOnCreatureEffects;
        private final Set<RoomEffectSource> useOnAreaEffects;
        private final List<RichOutput> useDisplayPages;
        private final int totalNumberUsableTimes;
        private final boolean equippingRequired;
        private final boolean selfOnly;
        private final CreatureFilterQuery userRestrictions;
        private final CreatureFilterQuery creatureFilter;
        private final ItemFilterQuery itemFilter;
        private int timesUsed = 0;

        public static UsableBuilder getBuilder() {
            return new UsableBuilder();
        }

        public static final class UsableBuilder {
            private Set<CreatureEffectSource.Builder> useOnCreatureEffects;
            private Set<RoomEffectSource.Builder> useOnAreaEffects;
            private List<RichOutputBuilder> useDisplayPages;
            private int totalNumberUsableTimes = -1;
            private boolean equippingRequired;
            private boolean selfOnly;
            private CreatureFilterQuery userRestrictions;
            private CreatureFilterQuery creatureFilter;
            private ItemFilterQuery itemFilter;
            private int timesUsed = 0;

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

            public Set<RoomEffectSource> getUseOnAreaEffects() {
                return useOnAreaEffects == null ? null
                        : useOnAreaEffects.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public UsableBuilder setUseOnAreaEffects(Set<RoomEffectSource.Builder> useOnAreaEffects) {
                this.useOnAreaEffects = useOnAreaEffects;
                return this;
            }

            public UsableBuilder addUseOnAreaEffect(RoomEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.useOnAreaEffects == null) {
                        this.useOnAreaEffects = new LinkedHashSet<>();
                    }
                    this.useOnAreaEffects.add(builder);
                }
                return this;
            }

            public UsableBuilder clearAreaEffects() {
                if (this.useOnAreaEffects != null) {
                    this.useOnAreaEffects.clear();
                }
                return this;
            }

            public List<RichOutput> getUseDisplayPages() {
                return useDisplayPages == null ? null
                        : useDisplayPages.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .toList();
            }

            public UsableBuilder setUseDisplayPages(List<RichOutput.RichOutputBuilder> useDisplayPages) {
                this.useDisplayPages = useDisplayPages;
                return this;
            }

            public UsableBuilder addUseDisplayPage(RichOutput.RichOutputBuilder nextPage) {
                if (nextPage != null) {
                    if (this.useDisplayPages == null) {
                        this.useDisplayPages = new ArrayList<>();
                    }
                    this.useDisplayPages.add(nextPage);
                }
                return this;
            }

            public UsableBuilder clearUseDisplayPages() {
                if (this.useDisplayPages != null) {
                    this.useDisplayPages.clear();
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
                return equippingRequired;
            }

            public UsableBuilder setEquippingRequired(boolean equippingRequired) {
                this.equippingRequired = equippingRequired;
                return this;
            }

            public boolean isSelfOnly() {
                return selfOnly;
            }

            public UsableBuilder setSelfOnly(boolean selfOnly) {
                this.selfOnly = selfOnly;
                return this;
            }

            public CreatureFilterQuery getUserRestrictions() {
                return userRestrictions;
            }

            public UsableBuilder setUserRestrictions(CreatureFilterQuery userRestrictions) {
                this.userRestrictions = userRestrictions;
                return this;
            }

            public CreatureFilterQuery getCreatureFilter() {
                return creatureFilter;
            }

            public UsableBuilder setCreatureFilter(CreatureFilterQuery creatureFilter) {
                this.creatureFilter = creatureFilter;
                return this;
            }

            public ItemFilterQuery getItemFilter() {
                return itemFilter;
            }

            public UsableBuilder setItemFilter(ItemFilterQuery itemFilter) {
                this.itemFilter = itemFilter;
                return this;
            }

            public int getTimesUsed() {
                return timesUsed;
            }

            public UsableBuilder setTimesUsed(int timesUsed) {
                this.timesUsed = timesUsed;
                return this;
            }

            public boolean requiresEquipping() {
                return this.equippingRequired;
            }

            public CreatureFilterQuery affectsCreaturesLike() {
                return this.creatureFilter;
            }

            public ItemFilterQuery affectsItemsLike() {
                return this.itemFilter;
            }

            public Usable build() {
                return new Usable(this);
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("UsableBuilder [useOnCreatureEffects=").append(useOnCreatureEffects)
                        .append(", useOnAreaEffects=").append(useOnAreaEffects).append(", useDisplayPages=")
                        .append(useDisplayPages).append(", totalNumberUsableTimes=").append(totalNumberUsableTimes)
                        .append(", equippingRequired=").append(equippingRequired).append(", selfOnly=").append(selfOnly)
                        .append(", userRestrictions=").append(userRestrictions).append(", creatureFilter=")
                        .append(creatureFilter).append(", itemFilter=").append(itemFilter).append(", timesUsed=")
                        .append(timesUsed).append("]");
                return builder.toString();
            }

        }

        private Usable() {
            this.totalNumberUsableTimes = -1;
            this.useOnAreaEffects = null;
            this.useOnCreatureEffects = null;
            this.useDisplayPages = null;
            this.timesUsed = 0;
            this.equippingRequired = false;
            this.selfOnly = false;
            this.userRestrictions = null;
            this.creatureFilter = null;
            this.itemFilter = null;
        }

        protected Usable(UsableBuilder builder) {
            if (builder == null) {
                this.totalNumberUsableTimes = -1;
                this.useOnAreaEffects = null;
                this.useOnCreatureEffects = null;
                this.useDisplayPages = null;
                this.timesUsed = 0;
                this.equippingRequired = false;
                this.selfOnly = false;
                this.userRestrictions = null;
                this.creatureFilter = null;
                this.itemFilter = null;
            } else {
                this.useOnCreatureEffects = builder.getUseOnCreatureEffects();
                this.useOnAreaEffects = builder.getUseOnAreaEffects();
                this.useDisplayPages = builder.getUseDisplayPages();
                this.totalNumberUsableTimes = builder.getTotalNumberUsableTimes();
                this.equippingRequired = builder.isEquippingRequired();
                this.selfOnly = builder.isSelfOnly();
                this.userRestrictions = builder.getUserRestrictions();
                this.creatureFilter = builder.getCreatureFilter();
                this.itemFilter = builder.getItemFilter();
                this.timesUsed = builder.getTimesUsed();
            }
        }

        public Usable(UsableCapability usable) {
            if (usable == null) {
                this.totalNumberUsableTimes = -1;
                this.useOnAreaEffects = null;
                this.useOnCreatureEffects = null;
                this.useDisplayPages = null;
                this.timesUsed = 0;
                this.equippingRequired = false;
                this.selfOnly = false;
                this.userRestrictions = null;
                this.creatureFilter = null;
                this.itemFilter = null;
            } else {
                this.totalNumberUsableTimes = usable.getTotalNumberUsableTimes();
                this.useOnAreaEffects = usable.getUseOnAreaEffects();
                this.useOnCreatureEffects = usable.getUseOnCreatureEffects();
                this.useDisplayPages = usable.getUseDisplayPages();
                this.timesUsed = 0;
                this.equippingRequired = usable.requiresEquipping();
                this.selfOnly = usable.isSelfOnly();
                this.userRestrictions = usable.getUserRestrictions();
                this.creatureFilter = usable.affectsCreaturesLike();
                this.itemFilter = usable.affectsItemsLike();
            }
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
            if (this.useOnCreatureEffects != null || !this.useOnCreatureEffects.isEmpty()) {
                sj.add("When used on a Creature, it has the following effects:");
                for (final CreatureEffectSource source : this.useOnCreatureEffects) {
                    sj.add(source.getDescription());
                }
            }
            if (this.useOnAreaEffects != null || !this.useOnAreaEffects.isEmpty()) {
                sj.add("When used on an Area or Room, it has the following effects:");
                for (final RoomEffectSource source : this.useOnAreaEffects) {
                    sj.add(source.getDescription());
                }
            }
            seeEventBuilder.addExtraInfo(sj.toString());
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
        public boolean requiresEquipping() {
            return this.equippingRequired;
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
        public synchronized boolean useOnce() {
            this.timesUsed++;
            return this.timesUsed < this.totalNumberUsableTimes;
        }

        @Override
        public CreatureFilterQuery getUserRestrictions() {
            return this.userRestrictions;
        }

        @Override
        public CreatureFilterQuery affectsCreaturesLike() {
            return this.creatureFilter;
        }

        @Override
        public ItemFilterQuery affectsItemsLike() {
            return this.itemFilter;
        }

        @Override
        public Set<CreatureEffectSource> getUseOnCreatureEffects() {
            return this.useOnCreatureEffects == null ? Set.of()
                    : Collections.unmodifiableSet(this.useOnCreatureEffects);
        }

        @Override
        public Set<RoomEffectSource> getUseOnAreaEffects() {
            return this.useOnAreaEffects == null ? Set.of() : Collections.unmodifiableSet(this.useOnAreaEffects);
        }

        @Override
        public List<RichOutput> getUseDisplayPages() {
            return this.useDisplayPages;
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "Usable [", "]");
            sj.add("timesUsed=" + Integer.toString(timesUsed));
            sj.add("totalNumberUsableTimes=" + Integer.toString(totalNumberUsableTimes));
            sj.add("equippingRequired=" + Boolean.toString(equippingRequired));
            sj.add("selfOnly=" + Boolean.toString(selfOnly));
            if (this.useOnCreatureEffects != null) {
                sj.add("useOnCreatureEffects=" + useOnCreatureEffects.toString());
            }
            if (this.useOnAreaEffects != null) {
                sj.add("useOnAreaEffects=" + useOnAreaEffects.toString());
            }
            if (this.useDisplayPages != null) {
                sj.add("useDisplayPages=" + this.useDisplayPages.toString());
            }
            if (this.userRestrictions != null) {
                sj.add("userRestrictions=" + this.userRestrictions.toString());
            }
            if (this.creatureFilter != null) {
                sj.add("creatureFilter=" + this.creatureFilter.toString());
            }
            if (this.itemFilter != null) {
                sj.add("itemFilter=" + this.itemFilter.toString());
            }

            return sj.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(useOnCreatureEffects, useOnAreaEffects, useDisplayPages, totalNumberUsableTimes,
                    equippingRequired, selfOnly, userRestrictions, creatureFilter, itemFilter);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Usable))
                return false;
            Usable other = (Usable) obj;
            return Objects.equals(useOnCreatureEffects, other.useOnCreatureEffects)
                    && Objects.equals(useOnAreaEffects, other.useOnAreaEffects)
                    && Objects.equals(useDisplayPages, other.useDisplayPages)
                    && totalNumberUsableTimes == other.totalNumberUsableTimes
                    && equippingRequired == other.equippingRequired && selfOnly == other.selfOnly
                    && Objects.equals(userRestrictions, other.userRestrictions)
                    && Objects.equals(creatureFilter, other.creatureFilter)
                    && Objects.equals(itemFilter, other.itemFilter);
        }

    }
}