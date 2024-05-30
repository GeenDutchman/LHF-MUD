package com.lhf.game.map;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.lhf.game.creature.CreatureNoOpVisitor;
import com.lhf.game.creature.CreatureVisitor;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.item.AItem;
import com.lhf.game.item.IItem;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.ItemNoOpVisitor;
import com.lhf.game.item.ItemVisitor;
import com.lhf.game.item.concrete.Item;
import com.lhf.game.map.Room.RoomBuilder;
import com.lhf.game.map.SubArea.ISubAreaBuildInfo;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.RoomAffectedEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.client.user.UserID;

public class InstancedArea implements Area {
    private final GameEventProcessorID gameEventProcessorID = new GameEventProcessorID();
    private final UUID uuid = gameEventProcessorID.getUuid();
    private final transient Logger logger;
    private final RoomBuilder builder;
    private final Map<UUID, Room> rooms;
    private transient CommandChainHandler successor;
    private final transient Land land;
    private final int limit;

    public static class InstancedAreaBuilder implements Area.AreaBuilder {
        private final String className;
        private final AreaBuilderID id;
        private Room.RoomBuilder subordinate;
        private int limit;

        private InstancedAreaBuilder() {
            this.className = this.getClass().getName();
            this.id = new AreaBuilderID();
            this.subordinate = RoomBuilder.getInstance().setName("Your Room")
                    .addForbiddenCommandType(AMessageType.DROP);
            this.limit = 1;
        }

        public static InstancedAreaBuilder getInstance() {
            return new InstancedAreaBuilder();
        }

        public int getLimit() {
            return limit;
        }

        public InstancedAreaBuilder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public AreaBuilderID getAreaBuilderID() {
            return this.id;
        }

        public Room.RoomBuilder getSubordinate() {
            if (this.subordinate == null) {
                this.subordinate = RoomBuilder.getInstance().setName("Your Room")
                        .addForbiddenCommandType(AMessageType.DROP);
            }
            return this.subordinate;
        }

        public InstancedAreaBuilder setName(String name) {
            this.getSubordinate().setName(name);
            return this;
        }

        public InstancedAreaBuilder setDescription(String description) {
            this.getSubordinate().setDescription(description);
            return this;
        }

        public InstancedAreaBuilder addItem(AItem item) {
            this.getSubordinate().addItem(item);
            return this;
        }

        public InstancedAreaBuilder addForbiddenCommandType(AMessageType type) {
            this.getSubordinate().addForbiddenCommandType(type);
            return this;
        }

        public InstancedAreaBuilder clearForbiddenCommandTypes() {
            this.getSubordinate().clearForbiddenCommandTypes();
            return this;
        }

        public InstancedAreaBuilder doNotForbidCommandType(AMessageType type) {
            this.getSubordinate().doNotForbidCommandType(type);
            return this;
        }

        @Override
        public Set<AMessageType> getForbiddenCommandTypes() {
            return this.getSubordinate().getForbiddenCommandTypes();
        }

        /**
         * This returns an empty list.
         * 
         * @deprecated because of creature collisions into and out of the InstancedArea
         */
        public Collection<INonPlayerCharacterBuildInfo> getNPCsToBuild() {
            return List.of();
        }

        public InstancedAreaBuilder addSubAreaBuilder(ISubAreaBuildInfo builder) {
            this.getSubordinate().addSubAreaBuilder(builder);
            return this;
        }

        public Collection<ISubAreaBuildInfo> getSubAreasToBuild() {
            return subordinate.getSubAreasToBuild();
        }

        public String getDescription() {
            return subordinate.getDescription();
        }

        public Collection<IItem> getItems() {
            return subordinate.getItems();
        }

        public String getName() {
            return subordinate.getName();
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof InstancedAreaBuilder))
                return false;
            InstancedAreaBuilder other = (InstancedAreaBuilder) obj;
            return Objects.equals(id, other.id);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("InstancedAreaBuilder [className=").append(className).append(", id=").append(id)
                    .append(", subordinate=").append(subordinate).append("]");
            return builder.toString();
        }

        @Override
        public InstancedArea build(CommandChainHandler successor, Land land, AIRunner aiRunner,
                ConversationManager conversationManager, boolean fallbackNoConversation) {
            return new InstancedArea(this, () -> land, () -> successor);
        }

    }

    public static InstancedAreaBuilder getBuilder() {
        return new InstancedAreaBuilder();
    }

    InstancedArea(InstancedAreaBuilder builder, Supplier<Land> landSupplier,
            Supplier<CommandChainHandler> successorSupplier) {
        this.builder = builder.getSubordinate();
        final String name = this.builder.getName();
        this.logger = Logger.getLogger(this.getClass().getName() + "."
                + (name != null && !name.isBlank() ? name.replaceAll("\\W", "_") : this.uuid.toString()));
        this.land = landSupplier.get();
        this.successor = successorSupplier.get();
        this.rooms = new LinkedHashMap<>();
        this.limit = builder.getLimit();
    }

    protected Collection<Room> getRooms() {
        return this.rooms.values();
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public Land getLand() {
        return this.land;
    }

    @Override
    public String getName() {
        return this.builder.getName();
    }

    @Override
    public Collection<ICreature> getCreatures() {
        return this.rooms.values().stream().flatMap(room -> room.getCreatures().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public synchronized boolean addCreature(ICreature creature) {
        AtomicBoolean addedCreature = new AtomicBoolean();

        CreatureVisitor adder = new CreatureNoOpVisitor() {
            @Override
            public void visit(Player player) {
                if (player == null) {
                    InstancedArea.this.log(Level.WARNING, "Cannot add null player");
                    return;
                }
                InstancedArea.this.log(Level.WARNING, () -> String.format("Trying to add '%s'", player.getName()));
                if (InstancedArea.this.rooms.isEmpty()) {
                    InstancedArea.this.log(Level.INFO, "Adding new room because there are no rooms");
                    Room built = InstancedArea.this.builder.build(InstancedArea.this.successor, InstancedArea.this.land,
                            null, null, false);
                    InstancedArea.this.rooms.put(built.getUuid(), built);
                }
                for (Room room : InstancedArea.this.rooms.values()) {
                    if (InstancedArea.this.limit <= 0 || room.getPlayers().size() < InstancedArea.this.limit) {
                        boolean added = room.addPlayer(player);
                        if (added) {
                            InstancedArea.this.log(Level.FINE, () -> String.format("Adding player '%s' to room '%s'",
                                    player.getName(), InstancedArea.this.uuid));
                            addedCreature.compareAndExchange(false, added);
                            return;
                        }
                    }
                }
                InstancedArea.this.log(Level.INFO,
                        () -> String.format("Adding new room for '%s' because there are no rooms within the limit",
                                player.getName()));
                Room built = InstancedArea.this.builder.build(InstancedArea.this.successor, InstancedArea.this.land,
                        null, null, false);
                InstancedArea.this.rooms.put(built.getUuid(), built);
                addedCreature.compareAndExchange(false, built.addPlayer(player));
            }

            @Override
            public void visit(SummonedNPC sNpc) {
                if (sNpc == null) {
                    InstancedArea.this.log(Level.WARNING, "Cannot add null summoned npc");
                    return;
                }
                final String summoner = sNpc.getLeaderName();
                for (Room room : InstancedArea.this.rooms.values()) {
                    final Collection<ICreature> inhabitants = room.filterCreatures(
                            EnumSet.of(CreatureFilters.NAME, CreatureFilters.TYPE), summoner, null, null, null,
                            Player.class, null);
                    if (inhabitants != null && inhabitants.size() == 1 && room.addCreature(sNpc)) {
                        InstancedArea.this.log(Level.FINE,
                                () -> String.format("Found a room that matches the summon's ('%s') summoner '%s'",
                                        sNpc.getName(), summoner));
                        addedCreature.compareAndExchange(false, true);
                        return;
                    }
                }
                InstancedArea.this.log(Level.WARNING,
                        () -> String.format("Found no rooms that match the summon's ('%s') summoner '%s'",
                                sNpc.getName(), summoner));

            }

            @Override
            public void visit(SummonedMonster sMonster) {
                if (sMonster == null) {
                    InstancedArea.this.log(Level.WARNING, "Cannot add null summoned monster");
                    return;
                }
                final String summoner = sMonster.getLeaderName();
                for (Room room : InstancedArea.this.rooms.values()) {
                    final Collection<ICreature> inhabitants = room.filterCreatures(
                            EnumSet.of(CreatureFilters.NAME, CreatureFilters.TYPE), summoner, null, null, null,
                            Player.class, null);
                    if (inhabitants != null && inhabitants.size() == 1 && room.addCreature(sMonster)) {
                        InstancedArea.this.log(Level.FINE,
                                () -> String.format("Found a room that matches the summon's ('%s') summoner '%s'",
                                        sMonster.getName(), summoner));
                        addedCreature.compareAndExchange(false, true);
                        return;
                    }
                }
                InstancedArea.this.log(Level.WARNING,
                        () -> String.format("Found no rooms that match the summon's ('%s') summoner '%s'",
                                sMonster.getName(), summoner));

            }
        };
        adder.accept(creature);
        return addedCreature.get();
    }

    @Override
    public synchronized Optional<ICreature> removeCreature(String name) {
        Optional<ICreature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public synchronized boolean removeCreature(ICreature creature) {
        boolean removed = false;
        Iterator<Entry<UUID, Room>> iterator = this.rooms.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<UUID, Room> next = iterator.next();
            Room room = next.getValue();
            if (room == null) {
                iterator.remove();
                continue;
            }
            removed |= room.removeCreature(creature);
            if (room.getCreatures().isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    @Override
    public synchronized boolean removeCreature(ICreature c, Directions dir) {
        boolean removed = false;
        Iterator<Entry<UUID, Room>> iterator = this.rooms.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<UUID, Room> next = iterator.next();
            Room room = next.getValue();
            if (room == null) {
                iterator.remove();
                continue;
            }
            removed |= room.removeCreature(c, dir);
            if (room.getCreatures().isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    @Override
    public Optional<Player> removePlayer(String name) {
        Optional<Player> found = this.getPlayer(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public Optional<Player> removePlayer(UserID id) {
        Optional<Player> toRemove = getPlayer(id);
        if (toRemove.isPresent()) {
            this.removeCreature(toRemove.get());
        }
        return toRemove;
    }

    @Override
    public boolean removePlayer(Player player) {
        return this.removeCreature(player);
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        boolean removed = false;
        Iterator<Entry<UUID, Room>> iterator = this.rooms.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<UUID, Room> next = iterator.next();
            Room room = next.getValue();
            if (room == null) {
                iterator.remove();
                continue;
            }
            removed |= room.onCreatureDeath(creature);
            if (room.getCreatures().isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    @Override
    public Collection<IItem> getItems() {
        return Collections.unmodifiableCollection(this.builder.getItems());
    }

    @Override
    public boolean addItem(IItem item) {
        if (item == null) {
            return false;
        }

        AtomicBoolean added = new AtomicBoolean();

        ItemVisitor visitor = new ItemNoOpVisitor() {
            @Override
            public void visit(Item note) {
                InstancedArea.this.builder.addItem(note);
                InstancedArea.this.rooms.values().forEach(room -> {
                    if (room != null) {
                        Item copied = note.makeCopy();
                        added.compareAndExchange(false, room.addItem(copied));
                    }
                });
            }

            @Override
            public void visit(InteractObject interactObject) {
                InstancedArea.this.builder.addItem(interactObject);
                InstancedArea.this.rooms.values().forEach(room -> {
                    if (room != null) {
                        InteractObject copied = interactObject.makeCopy();
                        boolean subAdded = room.addItem(copied);
                        added.compareAndExchange(false, subAdded);
                        if (subAdded) {
                            copied.setArea(room);
                        }
                    }
                });
            }
        };

        visitor.accept(item);

        return added.get();
    }

    @Override
    public boolean hasItem(String name) {
        return this.getItems().stream().anyMatch(item -> item != null && item.checkName(name));
    }

    @Override
    public Optional<IItem> removeItem(String name) {
        for (Iterator<IItem> iterator = this.builder.getItems().iterator(); iterator.hasNext();) {
            IItem item = iterator.next();
            if (item != null && item.checkName(name)) {
                iterator.remove();
                this.rooms.values().stream().forEach(room -> {
                    if (room != null) {
                        room.removeItem(name);
                    }
                });
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeItem(IItem item) {
        if (item == null) {
            return false;
        }
        AtomicBoolean removed = new AtomicBoolean();
        removed.compareAndExchange(false, this.builder.getItems().remove(item));
        this.rooms.values().stream().filter(room -> room != null)
                .forEach(room -> removed.compareAndExchange(false, room.removeItem(item)));
        return removed.get();
    }

    @Override
    public Iterator<? extends IItem> itemIterator() {
        return this.builder.getItems().iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("InstancedArea [uuid=").append(uuid).append(", builder=").append(builder).append(", rooms=")
                .append(rooms).append(", limit=").append(limit).append("]");
        return builder2.toString();
    }

    @Override
    public String getDescription() {
        return this.builder.getDescription();
    }

    @Override
    public SeeEvent produceMessage(boolean seeInvisible, boolean seeDirections) {
        return Area.super.produceMessage(seeInvisible, seeDirections);
    }

    @Override
    public RoomAffectedEvent processEffectApplication(RoomEffect effect) {
        this.logger.log(Level.WARNING,
                () -> String.format("Instanced Room NOT processing effect '%s'", effect.getName()));
        return null;
    }

    @Override
    public GameEvent processEffectRemoval(RoomEffect effect) {
        this.log(Level.INFO, () -> String.format("Currently room effects (among which '%s'), cannot really be removed",
                effect.getName()));
        return null;
    }

    @Override
    public GameEvent processEffectEvent(RoomEffect effect, GameEvent event) {
        this.log(Level.INFO,
                () -> String.format(
                        "Current room effects (among which '%s'), cannot really happen on any event (like '%s')",
                        effect.getName(), event.getXmlEventType()));
        return null;
    }

    @Override
    public NavigableSet<RoomEffect> getMutableEffects() {
        this.log(Level.WARNING, "Cannot get effects from InstancedArea!");
        return new TreeSet<>();
    }

    @Override
    public NavigableSet<SubArea> getSubAreas() {
        this.log(Level.WARNING, "Cannot get sub areas from InstancedArea!");
        return Collections.unmodifiableNavigableSet(new TreeSet<>());
    }

    @Override
    public boolean addSubArea(ISubAreaBuildInfo builder) {
        this.log(Level.WARNING, "Cannot add sub area to InstancedArea!");
        return false;
    }

    @Override
    public void acceptAreaVisitor(AreaVisitor visitor) {
        if (visitor != null) {
            this.rooms.values().stream().filter(room -> room != null).forEach(room -> visitor.visit(room));
        }
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        this.successor = successor;
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public GameEventProcessorID getEventProcessorID() {
        return this.gameEventProcessorID;
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        return Map.of();
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        // we are invisible
        return ctx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof InstancedArea))
            return false;
        InstancedArea other = (InstancedArea) obj;
        return Objects.equals(uuid, other.uuid);
    }

}
