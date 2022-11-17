package com.lhf.game.creature.intelligence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.server.client.ClientID;

// see https://gamedev.stackexchange.com/questions/12458/how-to-manage-all-the-npc-ai-objects-on-the-server/12512#12512

public class AIRunner implements Runnable {
    private BlockingQueue<ClientID> attentionQueue;
    private int chew;

    private class AIPair<T extends BasicAI> {
        public T ai;
        public AtomicBoolean queued;

        public AIPair(T ai) {
            this.ai = ai;
            this.queued = new AtomicBoolean(false);
        }

    }

    private Map<ClientID, AIPair<BasicAI>> aiMap;

    public AIRunner() {
        this.chew = 2;
        this.init();
    }

    public AIRunner(int chew) {
        this.chew = chew;
        this.init();
    }

    private void init() {
        if (this.attentionQueue == null) {
            this.attentionQueue = new LinkedBlockingQueue<>();
        }
        if (this.aiMap == null) {
            this.aiMap = new ConcurrentHashMap<>();
        }
    }

    protected void process(ClientID id) throws InterruptedException {
        if (id != null) {
            AIPair<BasicAI> aiPair = this.aiMap.get(id);
            if (aiPair != null) {
                aiPair.queued.set(false);
                while (chew <= 0 && aiPair.ai.peek() != null) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                for (int i = chew; i > 0 && aiPair.ai.peek() != null; i--) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                if (aiPair.ai.size() > 0) {
                    this.getAttention(id);
                }
            }
        }
    }

    public void getAttention(ClientID id) throws InterruptedException {
        AIPair<BasicAI> aPair = this.aiMap.get(id);
        if (aPair != null && aPair.queued.compareAndSet(false, true)) {
            // if aPair is defined and not queued
            this.attentionQueue.offer(id, 30, TimeUnit.SECONDS);
            aPair.queued.set(true);
        }
    }

    public void getAttention(BasicAI ai) throws InterruptedException {
        this.aiMap.computeIfAbsent(ai.getClientID(), clientId -> new AIPair<BasicAI>(ai));
        this.getAttention(ai.getClientID());
    }

    public BasicAI getQueuedAI(NonPlayerCharacter npc) {
        BasicAI qAi = new BasicAI(npc, this);
        if (this.aiMap == null) {
            this.aiMap = new HashMap<>();
        }
        this.aiMap.put(qAi.getClientID(), new AIPair<BasicAI>(qAi));
        return qAi;
    }

    public int size() {
        return this.attentionQueue.size();
    }

    protected ClientID getNext(long time, TimeUnit unit) throws InterruptedException {
        return this.attentionQueue.poll(time, unit);
    }

    @Override
    public void run() {
        while (true) {
            try {
                ClientID id = this.getNext(2, TimeUnit.MINUTES);
                if (id != null) {
                    this.process(id);
                } else {
                    for (ClientID iterId : this.aiMap.keySet()) {
                        this.process(iterId);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
