package com.lhf.game.creature.intelligence;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.creature.DungeonMaster;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.Monster;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.SummonedMonster;
import com.lhf.game.creature.SummonedNPC;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;

public class BasicAI extends Client {
    protected transient INonPlayerCharacter npc;
    protected BlockingQueue<GameEvent> queue;
    protected transient AIRunner runner;
    protected final transient Set<Class<? extends INonPlayerCharacter>> allowedSuccessorTypes;

    protected BasicAI(AIRunner runner) {
        super();
        this.allowedSuccessorTypes = new HashSet<>(Set.of(NonPlayerCharacter.class, DungeonMaster.class,
                SummonedNPC.class, Monster.class, SummonedMonster.class, INonPlayerCharacter.class));
        this.queue = new ArrayBlockingQueue<>(32, true);
        this.SetOut(new DoNothingSendStrategy());
        this.runner = runner;
    }

    public Set<Class<? extends INonPlayerCharacter>> getAllowedSuccessorTypes() {
        return allowedSuccessorTypes;
    }

    public GameEvent peek() {
        return this.queue.peek();
    }

    public GameEvent poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }

    public void process(GameEvent event) {
        if (this.npc == null) {
            return;
        }
        final Map<GameEventType, AIHandler> handlers = this.npc.getAIHandlers();
        if (handlers == null) {
            return;
        }
        if (event != null) {
            AIHandler ai = handlers.get(event.getEventType());
            if (ai != null) {
                ai.handle(this, event);
            } else {
                this.log(Level.WARNING,
                        () -> String.format("No handler found for %s: %s", event.getEventType(), event.print()));
            }
        }
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return super.getAcceptHook().andThen(event -> {
            try {
                if (this.runner == null) {
                    this.process(event);
                    return;
                }
                if (this.queue.offer(event, 30, TimeUnit.SECONDS)) {
                    this.runner.getAttention(this);
                } else {
                    this.log(Level.SEVERE, "Unable to queue: " + event.toString());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public INonPlayerCharacter getNpc() {
        return npc;
    }

    public void setNPC(INonPlayerCharacter nextNPC) {
        if (this.npc != null && nextNPC != null && this.npc.equals(nextNPC)) {
            return;
        }
        if (this.npc == null && nextNPC == null) {
            return;
        }
        this.log(Level.CONFIG, () -> String.format("Transitioning BasicAI %s from %s to back %s", this.getClientID(),
                this.npc, nextNPC));
        if (nextNPC == null && this.npc != null) {
            this.npc.log(Level.WARNING, () -> String.format("Controller %s detaching!", this.getClientID()));
            this.npc.setController(null); // detach controller
        }
        this.npc = nextNPC;
        if (this.npc != null) {
            this.npc.setController(this);
            this.updateLoggerSuffix(npc.getName());
        } else {
            this.updateLoggerSuffix(null);
        }
        super.setSuccessor(nextNPC);
    }

    @Override
    public void setSuccessor(CommandChainHandler successor) {
        if (successor == null) {
            this.setNPC(null); // just null of a different shape
        } else if (this.allowedSuccessorTypes.contains(successor.getClass())) {
            try {
                this.setNPC((INonPlayerCharacter) successor);
            } catch (ClassCastException e) {
                this.log(Level.SEVERE, String.format(
                        "Somehow the successor '%s' cannot be cast as an INonPlayerCharacter: %s", successor, e));
                throw e;
            }
        } else {
            final String errMessage = String.format("The successor '%s %s' is not one of the supprted types %s",
                    successor, successor.getClass(), this.allowedSuccessorTypes);
            this.log(Level.WARNING, errMessage);
            throw new IllegalArgumentException(errMessage);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BasicAI [npc=").append(npc).append(", queuesize=").append(this.queue != null ? queue.size() : 0)
                .append("]");
        return builder.toString();
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        String composed = this.toString() + ": " + logMessage;
        if (this.npc != null) {
            this.npc.log(logLevel, composed);
            return;
        }
        super.log(logLevel, composed);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        Supplier<String> composed = () -> this.toString()
                + (logMessageSupplier != null ? ": " + logMessageSupplier.get() : "");
        if (this.npc != null) {
            this.npc.log(logLevel, composed);
            return;
        }
        super.log(logLevel, composed);
    }

    @Override
    public synchronized void log(Level level, String msg, Throwable thrown) {
        String composed = this.toString() + ": " + msg;
        if (this.npc != null) {
            this.npc.log(level, composed, thrown);
            return;
        }
        super.log(level, msg, thrown);
    }

    @Override
    public synchronized void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        Supplier<String> composed = () -> this.toString() + (msgSupplier != null ? ": " + msgSupplier.get() : "");
        if (this.npc != null) {
            this.npc.log(level, thrown, composed);
            return;
        }
        super.log(level, thrown, msgSupplier);
    }

}
