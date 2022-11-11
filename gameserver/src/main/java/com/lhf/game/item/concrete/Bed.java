package com.lhf.game.item.concrete;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.game.map.Directions;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.out.BadGoMessage;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.BadGoMessage.BadGoType;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class Bed extends InteractObject implements MessageHandler {

    protected final ScheduledThreadPoolExecutor executor;
    protected final int sleepSeconds;
    protected Set<BedTime> occupants;
    protected Room room;

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

    public Bed(Room room, int capacity, int sleepSeconds) {
        super("Bed", true, true, "It's a bed.");
        this.sleepSeconds = Integer.max(sleepSeconds, 1);
        this.room = room;

        this.executor = new ScheduledThreadPoolExecutor(Integer.max(capacity, 1));
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.occupants = Collections.synchronizedSortedSet(new TreeSet<>());
        InteractAction sleepAction = (creature, triggerObject, args) -> {
            if (creature == null) {
                return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT);
            }
            if (this.getOccupancy() >= this.getCapacity()) {
                return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT, "The bed is full!");
            }

            BedTime bedTime = this.getBedTime(creature);
            if (bedTime == null) {
                bedTime = new BedTime(creature);
                bedTime.setFuture(this.executor.scheduleWithFixedDelay(bedTime, this.sleepSeconds, this.sleepSeconds,
                        TimeUnit.SECONDS));
                this.occupants.add(bedTime);

                return new InteractOutMessage(triggerObject, "You got in the bed!");
            }
            return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR, "You are already in the bed!");
        };
        this.setAction(sleepAction);
    }

    public int getCapacity() {
        return this.occupants.size();
    }

    public int getOccupancy() {
        return this.executor.getActiveCount();
    }

    protected boolean isInRoom(Creature creature) {
        if (this.room == null) {
            return false;
        }
        return this.room.containsCreature(creature);
    }

    protected BedTime getBedTime(Creature creature) {
        for (BedTime bedTime : this.occupants) {
            if (bedTime.occupant == creature) {
                return bedTime;
            }
        }
        return null;
    }

    public boolean remove(Creature doneSleeping) {
        BedTime found = this.getBedTime(doneSleeping);
        if (found != null) {
            found.occupant.sendMsg(new InteractOutMessage(this, "You got out of the bed!"));
            found.cancel();
            return this.occupants.remove(found);
        }
        return false;
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        // We only care about the room
        if (successor instanceof Room && successor != null) {
            this.room = (Room) room;
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
            this.remove(ctx.getCreature());
            if (this.room != null) {
                return this.room.handleMessage(ctx, msg);
            }
            return MessageHandler.super.handleMessage(ctx, msg);
        } else if (msg.getType() == CommandMessage.GO) {
            GoMessage goMessage = (GoMessage) msg;
            if (Directions.UP.equals(goMessage.getDirection())) {
                this.remove(ctx.getCreature());
                return true;
            } else {
                ctx.sendMsg(new BadGoMessage(BadGoType.DNE, goMessage.getDirection(), EnumSet.of(Directions.UP)));
                return true;
            }
        } else if (msg.getType() == CommandMessage.INTERACT) {
            InteractMessage interactMessage = (InteractMessage) msg;
            if (this.getName() == interactMessage.getObject()) {
                this.remove(ctx.getCreature());
                return true;
            }
        }
        return false;
    }

}
