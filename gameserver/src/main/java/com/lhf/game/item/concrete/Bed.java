package com.lhf.game.item.concrete;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.CreatureContainer;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.ICreature.CreatureCommandHandler;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.game.map.Area;
import com.lhf.game.map.Directions;
import com.lhf.messages.ClientID;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadGoEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemInteractionEvent;
import com.lhf.messages.events.BadGoEvent.BadGoType;
import com.lhf.messages.events.ItemInteractionEvent.InteractOutMessageType;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.server.client.user.UserID;

public class Bed extends InteractObject implements CreatureContainer, CommandChainHandler {
    protected final Logger logger;
    protected final ClientID clientID;
    protected final ScheduledThreadPoolExecutor executor;
    protected final int sleepSeconds;
    protected Set<BedTime> occupants;
    protected transient Area room;
    protected transient EnumMap<CommandMessage, CommandHandler> commands;

    protected class BedTime implements Runnable, Comparable<Bed.BedTime> {
        protected ICreature occupant;
        protected CommandChainHandler successor;
        protected ScheduledFuture<?> future;

        protected BedTime(ICreature occupant) {
            this.occupant = occupant;
            this.successor = occupant.getSuccessor();
            this.occupant.setSuccessor(Bed.this);
        }

        public BedTime setFuture(ScheduledFuture<?> future) {
            this.future = future;
            return this;
        }

        public boolean cancel() {
            if (this.future == null) {
                return false;
            }
            return this.future.cancel(true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(occupant);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BedTime)) {
                return false;
            }
            BedTime other = (BedTime) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
                return false;
            }
            return Objects.equals(occupant, other.occupant);
        }

        @Override
        public void run() {
            if (!this.getEnclosingInstance().isInRoom(occupant)) {
                this.future.cancel(false);
                this.getEnclosingInstance().occupants.remove(this);
            }
            EnumSet<Attributes> sleepAttrs = EnumSet.of(Attributes.CON, Attributes.INT);
            Attributes best = this.occupant.getHighestAttributeBonus(sleepAttrs);
            MultiRollResult sleepCheck = this.occupant.check(best);
            this.occupant.updateHitpoints(sleepCheck.getTotal());
            // TODO: regain spell energy?
            ItemInteractionEvent.Builder iom = ItemInteractionEvent.getBuilder().setPerformed()
                    .setDescription("You slept and got back " + sleepCheck.getColorTaggedName() + " hit points!")
                    .setTaggable(Bed.this);
            ICreature.eventAccepter.accept(this.occupant, iom.Build());
        }

        private Bed getEnclosingInstance() {
            return Bed.this;
        }

        @Override
        public int compareTo(BedTime o) {
            return this.occupant.compareTo(o.occupant);
        }
    }

    public static class Builder {
        private String name;
        private int sleepSeconds;
        private int capacity;
        private Set<ICreature> occupants;

        private Builder() {
            this.name = "Bed";
            this.sleepSeconds = 60;
            this.capacity = 1;
            this.occupants = new TreeSet<>();
        }

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder setName(String name) {
            this.name = name != null && !name.isBlank() ? name : "Bed";
            return this;
        }

        public Builder setSleepSeconds(int sleepSecs) {
            this.sleepSeconds = Integer.max(sleepSecs, 1);
            return this;
        }

        public Builder setCapacity(int cap) {
            this.capacity = Integer.max(cap, 1);
            return this;
        }

        public Builder addOccupant(ICreature occupant) {
            if (occupant != null) {
                if (this.occupants == null) {
                    this.occupants = new TreeSet<>();
                }
                this.occupants.add(occupant);
            }
            return this;
        }

        public Bed build(Area room) {
            Bed madeBed = new Bed(room, this);
            for (ICreature occupant : this.occupants) {
                madeBed.addCreature(occupant);
            }
            return madeBed;
        }
    }

    public Bed(Area room, Builder builder) {
        super(builder.name, true, true, "It's a bed.");
        this.clientID = new ClientID();
        this.logger = Logger.getLogger(this.getClass().getName() + "." + this.clientID.getUuid());
        this.sleepSeconds = builder.sleepSeconds;
        this.room = room;

        this.executor = new ScheduledThreadPoolExecutor(Integer.max(builder.capacity, 1));
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.occupants = Collections.synchronizedSortedSet(new TreeSet<>());
        for (ICreature alreadyThere : builder.occupants) {
            this.addCreature(alreadyThere);
        }

        InteractAction sleepAction = (creature, triggerObject, args) -> {
            return this.bedAction(creature, triggerObject, args);
        };
        this.setAction(sleepAction);
        this.commands = new EnumMap<>(CommandMessage.class);
        commands.put(CommandMessage.EXIT, new ExitHandler());
        commands.put(CommandMessage.GO, new GoHandler());
        commands.put(CommandMessage.INTERACT, new InteractHandler());
        commands.put(CommandMessage.SAY, new SayHandler());
        commands.put(CommandMessage.SHOUT, new ShoutHandler());
    }

    protected GameEvent bedAction(ICreature creature, InteractObject triggerObject, Map<String, Object> args) {
        if (creature == null) {
            return ItemInteractionEvent.getBuilder().setTaggable(triggerObject)
                    .setSubType(InteractOutMessageType.CANNOT)
                    .Build();
        }
        if (this.getOccupancy() >= this.getCapacity()) {
            this.logger.log(Level.WARNING,
                    () -> String.format("Over capacity! occupancy: %d capacity: %d", this.getOccupancy(),
                            this.getCapacity()));
            return ItemInteractionEvent.getBuilder().setTaggable(triggerObject)
                    .setSubType(InteractOutMessageType.CANNOT)
                    .setDescription("The bed is full!").Build();
        }

        if (this.addCreature(creature)) {
            return ItemInteractionEvent.getBuilder().setTaggable(triggerObject)
                    .setSubType(InteractOutMessageType.PERFORMED).setDescription("You are now in the bed!").Build();
        }
        return ItemInteractionEvent.getBuilder().setTaggable(triggerObject).setSubType(InteractOutMessageType.ERROR)
                .setDescription("You are already in the bed!").Build();
    }

    @Override
    public boolean addCreature(ICreature creature) {
        BedTime bedTime = this.getBedTime(creature);
        if (bedTime == null) {
            bedTime = new BedTime(creature);
            bedTime.setFuture(this.executor.scheduleWithFixedDelay(bedTime, this.sleepSeconds, this.sleepSeconds,
                    TimeUnit.SECONDS));
            this.logger.log(Level.FINER, () -> String.format("Creature '%s' getting in bed", creature.getName()));
            return this.occupants.add(bedTime);
        }
        return false;
    }

    public int getCapacity() {
        return this.executor.getCorePoolSize();
    }

    public int getOccupancy() {
        return this.occupants.size();
    }

    @Override
    public boolean addPlayer(Player player) {
        return this.addCreature(player);
    }

    @Override
    public Collection<ICreature> getCreatures() {
        Set<ICreature> creatures = new TreeSet<>();
        for (BedTime bedTime : this.occupants) {
            creatures.add(bedTime.occupant);
        }
        return Collections.unmodifiableSet(creatures);
    }

    @Override
    public boolean onCreatureDeath(ICreature creature) {
        boolean removed = this.removeCreature(creature);
        removed = this.room.onCreatureDeath(creature) || removed;
        return removed;
    }

    @Override
    public Optional<ICreature> removeCreature(String name) {
        Optional<ICreature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removeCreature(ICreature doneSleeping) {
        BedTime found = this.getBedTime(doneSleeping);
        if (found != null) {
            ICreature.eventAccepter.accept(found.occupant, ItemInteractionEvent.getBuilder().setTaggable(this)
                    .setDescription("You got out of the bed!").setPerformed().Build());
            found.cancel();
            found.occupant.setSuccessor(found.successor);
            this.logger.log(Level.FINER, () -> String.format("%s is done sleeping and will participate in %s",
                    doneSleeping.getName(), found.successor));
            return this.occupants.remove(found);
        }
        return false;
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
        Optional<Player> toRemove = this.getPlayer(id);
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
    public ArrayList<ICreature> getCreaturesLike(String creatureName) {
        ArrayList<ICreature> match = new ArrayList<>();
        ArrayList<ICreature> closeMatch = new ArrayList<>();

        for (BedTime bedTime : this.occupants) {
            ICreature c = bedTime.occupant;
            if (c.CheckNameRegex(creatureName, 3)) {
                match.add(c);
            }
            if (c.checkName(creatureName)) {
                closeMatch.add(c);
                return closeMatch;
            }
        }

        return match;
    }

    protected boolean isInRoom(ICreature creature) {
        if (this.room == null) {
            return false;
        }
        return this.room.hasCreature(creature);
    }

    protected BedTime getBedTime(ICreature creature) {
        for (BedTime bedTime : this.occupants) {
            if (bedTime.occupant == creature) {
                return bedTime;
            }
        }
        return null;
    }

    public boolean isInBed(ICreature creature) {
        return this.getBedTime(creature) != null;
    }

    @Override
    public ClientID getClientID() {
        return this.clientID;
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        // We only care about the room
        if (successor instanceof Area && successor != null) {
            this.room = (Area) successor;
        }
    }

    @Override
    public CommandChainHandler getSuccessor() {
        return null; // we're gonna pretend there *is* no successor!
    }

    @Override
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        return Collections.unmodifiableMap(this.commands);
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        this.logger.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        this.logger.log(logLevel, logMessageSupplier);
    }

    public interface BedCommandHandler extends CreatureCommandHandler {
        static final Predicate<CommandContext> defaultBedPredicate = BedCommandHandler.defaultCreaturePredicate;
    }

    /**
     * Reduces options to just "Go UP"
     */
    protected class GoHandler implements BedCommandHandler {
        private static final String helpString = "Use the command <command>GO UP</command> to get out of bed. ";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.GO;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(GoHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return GoHandler.defaultBedPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.GO && cmd instanceof GoMessage goMessage) {
                if (Directions.UP.equals(goMessage.getDirection())) {
                    Bed.this.removeCreature(ctx.getCreature());
                    return ctx.handled();
                } else {
                    ctx.receive(
                            BadGoEvent.getBuilder().setSubType(BadGoType.DNE).setAttempted(goMessage.getDirection())
                                    .setAvailable(EnumSet.of(Directions.UP)).Build());
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Bed.this;
        }

    }

    protected class ExitHandler implements BedCommandHandler {
        private static final String helpString = "Disconnect and leave Ibaif!";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.EXIT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(ExitHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ExitHandler.defaultBedPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.EXIT) {
                Bed.this.removeCreature(ctx.getCreature());
                if (Bed.this.room != null) {
                    return Bed.this.room.handleChain(ctx, cmd);
                }
                return CommandChainHandler.passUpChain(Bed.this, ctx, cmd);
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Bed.this;
        }

    }

    protected class InteractHandler implements BedCommandHandler {
        private final String helpString = "Use the command <command>INTERACT " + Bed.this.getName()
                + "</command> to get out of bed. ";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.INTERACT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(this.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return InteractHandler.defaultBedPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.INTERACT
                    && cmd instanceof InteractMessage interactMessage) {
                if (Bed.this.getName().equalsIgnoreCase(interactMessage.getObject())) {
                    Bed.this.removeCreature(ctx.getCreature());
                    return ctx.handled();
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Bed.this;
        }

    }

    protected class SayHandler implements BedCommandHandler {
        private static final String helpString = "Says stuff to the people in the area.";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SAY;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(SayHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ExitHandler.defaultBedPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SAY) {
                if (Bed.this.room != null) {
                    return Bed.this.room.handleChain(ctx, cmd);
                }
                return CommandChainHandler.passUpChain(Bed.this, ctx, cmd);
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Bed.this;
        }

    }

    protected class ShoutHandler implements BedCommandHandler {
        private static final String helpString = "Shouts stuff to the people in the land.";

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.SHOUT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(ShoutHandler.helpString);
        }

        @Override
        public Predicate<CommandContext> getEnabledPredicate() {
            return ExitHandler.defaultBedPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd != null && cmd.getType() == CommandMessage.SHOUT) {
                if (Bed.this.room != null) {
                    return Bed.this.room.handleChain(ctx, cmd);
                }
                return CommandChainHandler.passUpChain(Bed.this, ctx, cmd);
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler() {
            return Bed.this;
        }

    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

}
