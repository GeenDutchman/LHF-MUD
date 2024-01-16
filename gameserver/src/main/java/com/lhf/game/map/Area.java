package com.lhf.game.map;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;

import com.lhf.game.AffectableEntity;
import com.lhf.game.CreatureContainer;
import com.lhf.game.ItemContainer;
import com.lhf.game.creature.CreaturePartitionSetVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.IMonster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.Item;
import com.lhf.game.item.ItemPartitionListVisitor;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.NotableFixture;
import com.lhf.game.map.SubArea.SubAreaBuilder;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.game.map.commandHandlers.AreaAttackHandler;
import com.lhf.game.map.commandHandlers.AreaCastHandler;
import com.lhf.game.map.commandHandlers.AreaDropHandler;
import com.lhf.game.map.commandHandlers.AreaInteractHandler;
import com.lhf.game.map.commandHandlers.AreaRestHandler;
import com.lhf.game.map.commandHandlers.AreaSayHandler;
import com.lhf.game.map.commandHandlers.AreaSeeHandler;
import com.lhf.game.map.commandHandlers.AreaTakeHandler;
import com.lhf.game.map.commandHandlers.AreaUseHandler;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.GameEventProcessor;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.events.SeeEvent.SeeCategory;
import com.lhf.messages.in.AMessageType;

public interface Area
        extends ItemContainer, CreatureContainer, CommandChainHandler, Comparable<Area>, AffectableEntity<RoomEffect> {

    public interface AreaBuilder extends Serializable {

        @FunctionalInterface
        public static interface PostBuildRoomOperations<A extends Area> {
            public abstract void accept(A area) throws FileNotFoundException;

            public default PostBuildRoomOperations<A> andThen(PostBuildRoomOperations<? super A> after) {
                Objects.requireNonNull(after);
                return (t) -> {
                    this.accept(t);
                    after.accept(t);
                };
            }
        }

        public final static class AreaBuilderID implements Comparable<AreaBuilderID> {
            private final UUID id = UUID.randomUUID();

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
                if (!(obj instanceof AreaBuilderID))
                    return false;
                AreaBuilderID other = (AreaBuilderID) obj;
                return Objects.equals(id, other.id);
            }

            @Override
            public int compareTo(AreaBuilderID arg0) {
                return this.id.compareTo(arg0.id);
            }

            @Override
            public String toString() {
                return this.id.toString();
            }

        }

        public abstract AreaBuilderID getAreaBuilderID();

        public abstract String getName();

        public abstract String getDescription();

        public abstract Collection<Item> getItems();

        public abstract Collection<INonPlayerCharacter.AbstractNPCBuilder<?, ?>> getNPCsToBuild();

        public abstract Collection<SubAreaBuilder<?, ?>> getSubAreasToBuild();

        public abstract Area quickBuild(CommandChainHandler successor, Land land,
                AIRunner aiRunner);

        public abstract Area build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException;

        public default Area build(Land land, AIRunner aiRunner, StatblockManager statblockManager,
                ConversationManager conversationManager) throws FileNotFoundException {
            return this.build(land, land, aiRunner, statblockManager, conversationManager);
        }
    }

    public abstract UUID getUuid();

    public abstract boolean removeCreature(ICreature c, Directions dir);

    public abstract Land getLand();

    public abstract NavigableSet<SubArea> getSubAreas();

    public boolean addSubArea(SubAreaBuilder<?, ?> builder);

    public default SubArea getSubAreaForSort(SubAreaSort sort) {
        if (sort == null) {
            return null;
        }
        final NavigableSet<SubArea> subAreas = this.getSubAreas();
        if (subAreas == null) {
            return null;
        }
        for (final SubArea subArea : subAreas) {
            if (sort.equals(subArea.getSubAreaSort())) {
                return subArea;
            }
        }
        return null;
    }

    public default boolean hasSubAreaSort(SubAreaSort sort) {
        if (sort == null) {
            return false;
        }
        return this.getSubAreaForSort(sort) != null;
    }

    @Override
    default SeeEvent produceMessage() {
        return this.produceMessage(false, true);
    }

    default SeeEvent produceMessage(boolean seeInvisible, boolean seeDirections) {
        SeeEvent.Builder seen = SeeEvent.getBuilder().setExaminable(this);
        if (seeDirections) {
            if (this.getLand() == null) {
                seen.addExtraInfo("There is no apparent way out of here.");
            } else {
                Land land = this.getLand();
                Set<Directions> exits = land.getAreaExits(this);
                if (exits != null) {
                    for (Directions dir : exits) {
                        seen.addSeen(SeeCategory.DIRECTION, dir);
                    }
                }
            }
        }
        CreaturePartitionSetVisitor creatureVisitor = new CreaturePartitionSetVisitor();
        this.acceptCreatureVisitor(creatureVisitor);
        for (final Player player : creatureVisitor.getPlayers()) {
            seen.addSeen(SeeCategory.PLAYER, player);
        }
        for (final IMonster monster : creatureVisitor.getMonsters()) {
            seen.addSeen(SeeCategory.MONSTER, monster);
        }
        for (final IMonster monster : creatureVisitor.getSummonedMonsters()) {
            seen.addSeen(SeeCategory.MONSTER, monster);
        }
        for (final INonPlayerCharacter npc : creatureVisitor.getNpcs()) {
            seen.addSeen(SeeCategory.NPC, npc);
        }
        for (final INonPlayerCharacter npc : creatureVisitor.getSummonedNPCs()) {
            seen.addSeen(SeeCategory.NPC, npc);
        }

        ItemPartitionListVisitor itemVisitor = new ItemPartitionListVisitor();
        this.acceptItemVisitor(itemVisitor);
        for (final Takeable item : itemVisitor.getTakeables()) {
            seen.addSeen(item.isVisible() ? SeeCategory.TAKEABLE : SeeCategory.INVISIBLE_TAKEABLE,
                    item);
        }
        for (final NotableFixture item : itemVisitor.getNotes()) {
            seen.addSeen(item.isVisible() ? SeeCategory.ROOM_ITEM : SeeCategory.INVISIBLE_ROOM_ITEM,
                    item);
        }
        for (final InteractObject item : itemVisitor.getInteractObjects()) {
            seen.addSeen(item.isVisible() ? SeeCategory.ROOM_ITEM : SeeCategory.INVISIBLE_ROOM_ITEM,
                    item);
        }

        return produceMessage(seen);
    }

    public interface AreaCommandHandler extends CommandHandler {

        final static String inBattleString = "You appear to be in a fight, so you cannot do that.";

        final static EnumMap<AMessageType, CommandHandler> areaCommandHandlers = new EnumMap<>(
                Map.of(AMessageType.ATTACK, new AreaAttackHandler(),
                        AMessageType.CAST, new AreaCastHandler(),
                        AMessageType.DROP, new AreaDropHandler(),
                        AMessageType.INTERACT, new AreaInteractHandler(),
                        AMessageType.REST, new AreaRestHandler(),
                        AMessageType.SAY, new AreaSayHandler(),
                        AMessageType.SEE, new AreaSeeHandler(),
                        AMessageType.TAKE, new AreaTakeHandler(),
                        AMessageType.USE, new AreaUseHandler()));

        @Override
        public default boolean isEnabled(CommandContext ctx) {
            if (ctx == null) {
                return false;
            }
            ICreature creature = ctx.getCreature();
            if (creature == null || !creature.isAlive()) {
                return false;
            }
            return ctx.getArea() != null;
        }

        @Override
        default CommandChainHandler getChainHandler(CommandContext ctx) {
            return ctx.getArea();
        }
    }

    @Override
    public default Collection<GameEventProcessor> getGameEventProcessors() {
        TreeSet<GameEventProcessor> messengers = new TreeSet<>(GameEventProcessor.getComparator());

        this.getCreatures().stream()
                .filter(creature -> creature != null).forEach(messenger -> messengers.add(messenger));

        return Collections.unmodifiableCollection(messengers);
    }

    @Override
    public default Consumer<GameEvent> getAcceptHook() {
        return (event) -> {
            if (event == null) {
                return;
            }
            if (event instanceof ITickEvent tickEvent) {
                this.tick(tickEvent);
            }
            this.announceDirect(event, this.getGameEventProcessors());
        };
    }

    @Override
    public default int compareTo(Area o) {
        if (this.equals(o)) {
            return 0;
        }
        int nameCompare = this.getName().compareTo(o.getName());
        if (nameCompare != 0) {
            return nameCompare;
        }
        return this.getUuid().compareTo(o.getUuid());
    }

    @Override
    default CommandContext addSelfToContext(CommandContext ctx) {
        if (ctx.getArea() == null) {
            ctx.setArea(this);
        }
        return ctx;
    }

    @Override
    public default String getStartTag() {
        return "<area>";
    }

    @Override
    public default String getEndTag() {
        return "</area>";
    }

    @Override
    public default String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }
}
