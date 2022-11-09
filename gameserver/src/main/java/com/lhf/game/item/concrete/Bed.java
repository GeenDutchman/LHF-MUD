package com.lhf.game.item.concrete;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TreeSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.item.InteractObject;
import com.lhf.game.item.interfaces.InteractAction;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;

public class Bed extends InteractObject implements MessageHandler {

    protected final ScheduledThreadPoolExecutor executor;
    protected Set<BedTime> occupants;

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

    public Bed(int capacity) {
        super("Bed", true, true, "It's a bed.");

        this.executor = new ScheduledThreadPoolExecutor(Integer.max(capacity, 1));
        this.executor.setRemoveOnCancelPolicy(true);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        this.occupants = new TreeSet<>();
        InteractAction sleepAction = (creature, triggerObject, args) -> {
            if (creature == null) {
                return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT);
            }
            if (this.getOccupancy() >= this.getCapacity()) {
                return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT, "The bed is full!");
            }

            BedTime bedTime = new BedTime(creature);
            bedTime.setFuture(this.executor.scheduleWithFixedDelay(bedTime, 30, 30, TimeUnit.SECONDS));
            this.occupants.add(bedTime);

            return new InteractOutMessage(triggerObject, "You got in the bed!");
        };
        this.setAction(sleepAction);
    }

    public int getCapacity() {
        return this.occupants.size();
    }

    public int getOccupancy() {
        return this.executor.getActiveCount();
    }

    public boolean remove(Creature doneSleeping) {
        BedTime found = null;
        for (BedTime bedTime : this.occupants) {
            if (bedTime.occupant == doneSleeping) {
                found = bedTime;
                break;
            }
        }
        if (found != null) {
            found.cancel();
            return this.occupants.remove(found);
        }
        return false;
    }

}
