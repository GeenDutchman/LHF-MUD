package com.lhf.game.creature.intelligence;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.intelligence.handlers.BattleTurnHandler;
import com.lhf.game.creature.intelligence.handlers.ForgetOnOtherExit;
import com.lhf.game.creature.intelligence.handlers.HandleCreatureAffected;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.FleeMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;
import com.lhf.server.interfaces.NotNull;

public class BasicAI extends Client {
    protected NonPlayerCharacter npc;
    protected Map<OutMessageType, AIChunk> handlers;
    protected BlockingQueue<OutMessage> queue;
    protected AIRunner runner;

    protected BasicAI(NonPlayerCharacter npc, AIRunner runner) {
        super();
        this.npc = npc;
        this.npc.setController(this);
        this.setSuccessor(npc);
        this.SetOut(new DoNothingSendStrategy());
        this.handlers = new TreeMap<>();
        this.initBasicHandlers();
        this.runner = runner;
        this.queue = new ArrayBlockingQueue<>(32, true);
    }

    public OutMessage peek() {
        return this.queue.peek();
    }

    public OutMessage poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }

    public void process(OutMessage msg) {
        if (msg != null) {
            AIChunk ai = this.handlers.get(msg.getOutType());
            if (ai != null) {
                ai.handle(this, msg);
            } else {
                this.log(Level.WARNING, () -> String.format("No handler found for %s: %s",
                        msg.getOutType(), msg.print()));
            }
        }
    }

    private void initBasicHandlers() {
        if (this.handlers == null) {
            this.handlers = new TreeMap<>();
        }
        this.handlers.put(OutMessageType.FIGHT_OVER, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.FIGHT_OVER) && bai.getNpc().isInBattle()) {
                bai.npc.getHarmMemories().reset();
            }
        });

        this.handlers.put(OutMessageType.FLEE, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.FLEE)) {
                FleeMessage flee = (FleeMessage) msg;
                if (flee.isFled() && flee.getRunner() != null) {
                    if (flee.getRunner() == bai.getNpc()) {
                        bai.npc.getHarmMemories().reset();
                    }
                }
            }
        });
        this.handlers.put(OutMessageType.BAD_TARGET_SELECTED, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BAD_TARGET_SELECTED) && bai.getNpc().isInBattle()) {
                BadTargetSelectedMessage btsm = (BadTargetSelectedMessage) msg;
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

    public BasicAI addHandler(OutMessageType type, AIChunk chunk) {
        this.handlers.put(type, chunk);
        return this;
    }

    public BasicAI addHandler(@NotNull AIHandler aiHandler) {
        this.handlers.put(aiHandler.getOutMessageType(), aiHandler);
        return this;
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        super.sendMsg(msg);
        try {
            if (this.runner == null) {
                this.process(msg);
                return;
            }
            if (this.queue.offer(msg, 30, TimeUnit.SECONDS)) {
                this.runner.getAttention(this);
            } else {
                this.log(Level.SEVERE, "Unable to queue: " + msg.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public NonPlayerCharacter getNpc() {
        return npc;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BasicAI [npc=").append(npc).append(", queuesize=").append(queue.size()).append("]");
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
