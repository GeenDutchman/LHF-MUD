package com.lhf.game.creature.intelligence;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.intelligence.handlers.BattleTurnHandler;
import com.lhf.game.creature.intelligence.handlers.ForgetOnOtherExit;
import com.lhf.game.creature.intelligence.handlers.HandleCreatureAffected;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.messages.GameEventType;
import com.lhf.messages.out.BadTargetSelectedEvent;
import com.lhf.messages.out.CreatureFledEvent;
import com.lhf.messages.out.GameEvent;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;
import com.lhf.server.interfaces.NotNull;

public class BasicAI extends Client {
    protected INonPlayerCharacter npc;
    protected Map<GameEventType, AIChunk> handlers;
    protected BlockingQueue<GameEvent> queue;
    protected AIRunner runner;

    protected BasicAI(INonPlayerCharacter npc, AIRunner runner) {
        super();
        this.queue = new ArrayBlockingQueue<>(32, true);
        this.npc = npc;
        if (npc != null) {
            this.logger = Logger.getLogger(this.logger.getName() + "." + npc.getName().replaceAll("\\W", "_"));
        }
        this.npc.setController(this);
        this.setSuccessor(npc);
        this.SetOut(new DoNothingSendStrategy());
        this.handlers = new TreeMap<>();
        this.initBasicHandlers();
        this.runner = runner;
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

    public void process(GameEvent msg) {
        if (msg != null) {
            AIChunk ai = this.handlers.get(msg.getEventType());
            if (ai != null) {
                ai.handle(this, msg);
            } else {
                this.log(Level.WARNING, () -> String.format("No handler found for %s: %s",
                        msg.getEventType(), msg.print()));
            }
        }
    }

    private void initBasicHandlers() {
        if (this.handlers == null) {
            this.handlers = new TreeMap<>();
        }
        this.handlers.put(GameEventType.FIGHT_OVER, (BasicAI bai, GameEvent msg) -> {
            if (msg.getEventType().equals(GameEventType.FIGHT_OVER) && bai.getNpc().isInBattle()) {
                bai.npc.getHarmMemories().reset();
            }
        });

        this.handlers.put(GameEventType.FLEE, (BasicAI bai, GameEvent msg) -> {
            if (msg.getEventType().equals(GameEventType.FLEE)) {
                CreatureFledEvent flee = (CreatureFledEvent) msg;
                if (flee.isFled() && flee.getRunner() != null) {
                    if (flee.getRunner() == bai.getNpc()) {
                        bai.npc.getHarmMemories().reset();
                    }
                }
            }
        });
        this.handlers.put(GameEventType.BAD_TARGET_SELECTED, (BasicAI bai, GameEvent msg) -> {
            if (msg.getEventType().equals(GameEventType.BAD_TARGET_SELECTED) && bai.getNpc().isInBattle()) {
                BadTargetSelectedEvent btsm = (BadTargetSelectedEvent) msg;
                this.log(Level.WARNING,
                        () -> String.format("Selected a bad target: %s with possible targets", btsm,
                                btsm.getPossibleTargets()));
            }
        });

        this.addHandler(new BattleTurnHandler());
        this.addHandler(new SpokenPromptChunk());
        this.addHandler(new ForgetOnOtherExit());
        this.addHandler(new HandleCreatureAffected());
        this.addHandler(new LewdAIHandler().setPartnersOnly());
    }

    public BasicAI addHandler(GameEventType type, AIChunk chunk) {
        this.handlers.put(type, chunk);
        return this;
    }

    public BasicAI addHandler(@NotNull AIHandler aiHandler) {
        this.handlers.put(aiHandler.getOutMessageType(), aiHandler);
        return this;
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

}
