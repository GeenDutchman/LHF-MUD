package com.lhf.game.item.concrete;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lhf.game.CreatureContainerMessageHandler;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.game.map.Area;
import com.lhf.game.map.Directions;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.out.BadGoMessage;
import com.lhf.messages.out.BadGoMessage.BadGoType;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.user.UserID;

public class Bed extends InteractObject implements CreatureContainerMessageHandler {

    protected final ScheduledThreadPoolExecutor executor;
    protected final int sleepSeconds;
    protected Set<BedTime> occupants;
    protected Area room;

    protected class BedTime implements Runnable, Comparable<Bed.BedTime> {
        protected Creature occupant;
        protected MessageHandler successor;
        protected ScheduledFuture<?> future;

        protected BedTime(Creature occupant) {
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
            InteractOutMessage iom = new InteractOutMessage(Bed.this,
                    "You slept and got back " + sleepCheck.getColorTaggedName() + " hit points!");
            this.occupant.sendMsg(iom);
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
        private Set<Creature> occupants;

        private Builder() {
            this.name = "Bed";
            this.sleepSeconds = 1;
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

        public Builder addOccupant(Creature occupant) {
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
            for (Creature occupant : this.occupants) {
                madeBed.addCreature(occupant);
            }
            return madeBed;
        }
    }

    public Bed(Area room, Builder builder) {
        super(builder.name, true, true, "It's a bed.");
        this.sleepSeconds = builder.sleepSeconds;
        this.room = room;

        this.executor = new ScheduledThreadPoolExecutor(Integer.max(builder.capacity, 1));
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.occupants = Collections.synchronizedSortedSet(new TreeSet<>());
        InteractAction sleepAction = (creature, triggerObject, args) -> {
            return this.bedAction(creature, triggerObject, args);
        };
        this.setAction(sleepAction);
    }

    protected OutMessage bedAction(Creature creature, InteractObject triggerObject, Map<String, Object> args) {
        if (creature == null) {
            return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT);
        }
        if (this.getOccupancy() >= this.getCapacity()) {
            return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT, "The bed is full!");
        }

        if (this.addCreature(creature)) {
            return new InteractOutMessage(triggerObject, "You are now in the bed!");
        }
        return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR, "You are already in the bed!");
    }

    @Override
    public boolean addCreature(Creature creature) {
        BedTime bedTime = this.getBedTime(creature);
        if (bedTime == null) {
            bedTime = new BedTime(creature);
            bedTime.setFuture(this.executor.scheduleWithFixedDelay(bedTime, this.sleepSeconds, this.sleepSeconds,
                    TimeUnit.SECONDS));
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
    public Collection<Creature> getCreatures() {
        Set<Creature> creatures = new TreeSet<>();
        for (BedTime bedTime : this.occupants) {
            creatures.add(bedTime.occupant);
        }
        return Collections.unmodifiableSet(creatures);
    }

    @Override
    public boolean onCreatureDeath(Creature creature) {
        boolean removed = this.removeCreature(creature);
        removed = this.room.onCreatureDeath(creature) || removed;
        return removed;
    }

    @Override
    public Optional<Creature> removeCreature(String name) {
        Optional<Creature> found = this.getCreature(name);
        if (found.isPresent()) {
            this.removeCreature(found.get());
        }
        return found;
    }

    @Override
    public boolean removeCreature(Creature doneSleeping) {
        BedTime found = this.getBedTime(doneSleeping);
        if (found != null) {
            found.occupant.sendMsg(new InteractOutMessage(this, "You got out of the bed!"));
            found.cancel();
            found.occupant.setSuccessor(found.successor);
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
    public ArrayList<Creature> getCreaturesLike(String creatureName) {
        ArrayList<Creature> match = new ArrayList<>();
        ArrayList<Creature> closeMatch = new ArrayList<>();

        for (BedTime bedTime : this.occupants) {
            Creature c = bedTime.occupant;
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

    protected boolean isInRoom(Creature creature) {
        if (this.room == null) {
            return false;
        }
        return this.room.hasCreature(creature);
    }

    protected BedTime getBedTime(Creature creature) {
        for (BedTime bedTime : this.occupants) {
            if (bedTime.occupant == creature) {
                return bedTime;
            }
        }
        return null;
    }

    public boolean isInBed(Creature creature) {
        return this.getBedTime(creature) != null;
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        // We only care about the room
        if (successor instanceof Area && successor != null) {
            this.room = (Area) successor;
        }
    }

    @Override
    public MessageHandler getSuccessor() {
        return null; // we're gonna pretend there *is* no successor!
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        EnumMap<CommandMessage, String> commands = new EnumMap<>(CommandMessage.class);
        commands.put(CommandMessage.EXIT, "Disconnect and leave Ibaif!");
        commands.put(CommandMessage.GO, "Use the command <command>GO UP</command> to get out of bed. ");
        commands.put(CommandMessage.INTERACT,
                "Use the command <command>INTERACT " + this.getName() + "</command> to get out of bed. ");
        return commands;
    }

    @Override
    public CommandContext addSelfToContext(CommandContext ctx) {
        return ctx;
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.EXIT) {
            this.removeCreature(ctx.getCreature());
            if (this.room != null) {
                return this.room.handleMessage(ctx, msg);
            }
            return CreatureContainerMessageHandler.super.handleMessage(ctx, msg);
        } else if (msg.getType() == CommandMessage.GO) {
            GoMessage goMessage = (GoMessage) msg;
            if (Directions.UP.equals(goMessage.getDirection())) {
                this.removeCreature(ctx.getCreature());
                return true;
            } else {
                ctx.sendMsg(new BadGoMessage(BadGoType.DNE, goMessage.getDirection(), EnumSet.of(Directions.UP)));
                return true;
            }
        } else if (msg.getType() == CommandMessage.INTERACT) {
            InteractMessage interactMessage = (InteractMessage) msg;
            if (this.getName() == interactMessage.getObject()) {
                this.removeCreature(ctx.getCreature());
                return true;
            }
        } else if (msg.getType() == CommandMessage.SAY || msg.getType() == CommandMessage.SHOUT) {
            if (this.room != null) {
                return this.room.handleMessage(ctx, msg);
            }
            return CreatureContainerMessageHandler.super.handleMessage(ctx, msg);
        }
        return false;
    }

}
