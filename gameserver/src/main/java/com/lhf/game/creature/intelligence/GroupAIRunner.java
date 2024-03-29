package com.lhf.game.creature.intelligence;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.server.client.Client.ClientID;

// see https://gamedev.stackexchange.com/questions/12458/how-to-manage-all-the-npc-ai-objects-on-the-server/12512#12512

public class GroupAIRunner implements AIRunner {
    private BlockingQueue<ClientID> attentionQueue;
    private final int chew;
    private volatile boolean stopit;
    private transient Logger logger;
    private long timeCount;
    private TimeUnit timeUnit;

    private class AIPair<T extends BasicAI> {
        public T ai;
        public AtomicBoolean queued;

        public AIPair(T ai) {
            this.ai = ai;
            this.queued = new AtomicBoolean(false);
        }

        @Override
        public String toString() {
            return this.ai.toString() + this.queued.toString();
        }
    }

    private Map<ClientID, AIPair<BasicAI>> aiMap;
    private volatile Thread myThread;

    public GroupAIRunner(boolean asThread) {
        this.chew = 2;
        this.timeCount = 5000;
        this.timeUnit = TimeUnit.MILLISECONDS;
        this.init(asThread);
    }

    public GroupAIRunner(boolean asThread, int chew) {
        this.chew = chew;
        this.timeCount = 5000;
        this.timeUnit = TimeUnit.MILLISECONDS;
        this.init(asThread);
    }

    public GroupAIRunner(boolean asThread, int chew, long timeCount, TimeUnit timeUnit) {
        this.chew = chew;
        this.timeCount = timeCount;
        this.timeUnit = timeUnit;
        this.init(asThread);
    }

    private void init(boolean asThread) {
        this.logger = Logger.getLogger(this.getClass().getName());
        this.logger.setLevel(Level.OFF);
        if (this.attentionQueue == null) {
            this.attentionQueue = new LinkedBlockingQueue<>();
        }
        if (this.aiMap == null) {
            this.aiMap = new ConcurrentHashMap<>();
        }
        this.stopit = false;
        if (asThread) {
            this.start();
        } else {
            this.logger.log(Level.INFO, "Not initializing as a thread");
        }
    }

    public GroupAIRunner start() {
        if (this.myThread == null) {
            this.logger.log(Level.INFO, "Starting a thread");
            this.myThread = new Thread(this);
            this.myThread.start();
        } else {
            this.logger.log(Level.INFO, "Already started as a thread");
        }
        return this;
    }

    @Override
    public BasicAI produceAI() {
        BasicAI ai = new BasicAI(this);
        this.logger.log(Level.FINE, "Producing an AI " + ai.getClientID().toString());
        this.aiMap.put(ai.getClientID(), new AIPair<BasicAI>(ai));
        return ai;
    }

    protected void process(ClientID id) throws InterruptedException {
        if (id != null) {
            AIPair<BasicAI> aiPair = this.aiMap.get(id);
            if (aiPair != null) {
                this.logger.log(Level.FINEST, "Processing for " + aiPair.ai.toString());
                aiPair.queued.set(false);
                while (this.getChew() <= 0 && aiPair.ai.peek() != null) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                for (int i = this.getChew(); i > 0 && aiPair.ai.peek() != null; i--) {
                    aiPair.ai.process(aiPair.ai.poll());
                }
                if (aiPair.ai.size() > 0) {
                    this.logger.log(Level.FINEST,
                            () -> String.format("%s still has messages enqueued", aiPair.ai.toString()));
                    this.getAttention(id);
                } else if (aiPair.ai.size() <= 0 && aiPair.ai.npc != null && !aiPair.ai.npc.isAlive()) {
                    // disconnect and garbage
                    aiPair.ai.setNPC(null);
                    this.aiMap.remove(id);
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
            this.logger.log(Level.FINEST, () -> String.format("Attention gotten for ai %s", aPair.ai.toString()));
        }
    }

    @Override
    public synchronized void getAttention(BasicAI ai) throws InterruptedException {
        this.logger.entering(this.getClass().getName(), "getAttention(BasicAI)", ai.toString());
        this.aiMap.computeIfAbsent(ai.getClientID(), clientID -> new AIPair<BasicAI>(ai));
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
        this.logger.entering(this.getClass().getName(), "run()", "running");
        while (!this.stopit) {
            try {
                this.logger.log(Level.FINER, () -> String.format("Polling for the next attention getter for %d %s",
                        this.getTimeCount(), this.getTimeUnit().toString()));
                ClientID id = this.getNext(this.getTimeCount(), this.getTimeUnit());
                if (id != null) {
                    this.process(id);
                } else {
                    this.logger.log(Level.FINER, "Checking to see if anyone has needs but has not asked for attention");
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
        this.logger.log(Level.INFO, "Signalling to stop");
        this.stopit = true;
    }

    @Override
    public boolean isStopped() {
        return !this.myThread.isAlive();
    }

    public long getTimeCount() {
        return timeCount;
    }

    public void setTimeCount(long timeCount) {
        this.timeCount = timeCount;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

}
