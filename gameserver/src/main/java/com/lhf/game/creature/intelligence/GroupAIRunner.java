package com.lhf.game.creature.intelligence;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.server.client.ClientID;

// see https://gamedev.stackexchange.com/questions/12458/how-to-manage-all-the-npc-ai-objects-on-the-server/12512#12512

public class GroupAIRunner implements AIRunner {
    private BlockingQueue<ClientID> attentionQueue;
    private final int chew;
    private volatile boolean stopit;
    private volatile boolean isStopped;

    private class AIPair<T extends BasicAI> {
        public T ai;
        public AtomicBoolean queued;

        public AIPair(T ai) {
            this.ai = ai;
            this.queued = new AtomicBoolean(false);
        }

    }

    private Map<ClientID, AIPair<BasicAI>> aiMap;

    public GroupAIRunner() {
        this.chew = 2;
        this.init();
    }

    public GroupAIRunner(int chew) {
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
        this.stopit = false;
        this.isStopped = false;
    }

    public synchronized BasicAI register(NonPlayerCharacter npc, AIHandler... handlers) {
        if (npc.getController() == null) {
            BasicAI basicAI = new BasicAI(npc, this);
            this.aiMap.put(basicAI.getClientID(), new AIPair<BasicAI>(basicAI));
            npc.setController(basicAI);
        }
        if (npc.getController() instanceof BasicAI) {
            BasicAI basicAI = (BasicAI) npc.getController();
            for (AIHandler handler : handlers) {
                basicAI.addHandler(handler);
            }
            return basicAI;
        }
        return null;
    }

    protected void process(ClientID id) throws InterruptedException {
        if (id != null) {
            AIPair<BasicAI> aiPair = this.aiMap.get(id);
            if (aiPair != null) {
                aiPair.queued.set(false);
                while (this.getChew() <= 0 && aiPair.ai.peek() != null) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                for (int i = this.getChew(); i > 0 && aiPair.ai.peek() != null; i--) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                if (aiPair.ai.size() > 0) {
                    this.getAttention(id);
                }
            }
        }
    }

    public synchronized void getAttention(ClientID id) throws InterruptedException {
        AIPair<BasicAI> aPair = this.aiMap.get(id);
        if (aPair != null && aPair.queued.compareAndSet(false, true)) {
            // if aPair is defined and not queued
            this.attentionQueue.offer(id, 30, TimeUnit.SECONDS);
            aPair.queued.set(true);
        }
    }

    @Override
    public synchronized void getAttention(BasicAI ai) throws InterruptedException {
        this.aiMap.computeIfAbsent(ai.getClientID(), clientId -> new AIPair<BasicAI>(ai));
        this.getAttention(ai.getClientID());
    }

    public int size() {
        return this.attentionQueue.size();
    }

    protected ClientID getNext(long time, TimeUnit unit) throws InterruptedException {
        return this.attentionQueue.poll(time, unit);
    }

    @Override
    public void run() {
        while (!this.stopit) {
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

    @Override
    public int getChew() {
        return this.chew;
    }

    @Override
    public void stopIt() {
        this.stopit = true;
    }

    @Override
    public boolean isStopped() {
        return this.isStopped;
    }

}
