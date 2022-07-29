package com.lhf.game.creature.intelligence;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.out.OutMessage;

public class QueuedAI extends BasicAI {
    private BlockingQueue<OutMessage> queue;
    private AIRunner runner;

    public QueuedAI(NonPlayerCharacter npc, AIRunner runner) {
        super(npc);
        this.queue = new ArrayBlockingQueue<>(32, true);
        this.runner = runner;
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
            }
        }
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        super.sendMsg(msg);
        try {
            this.queue.offer(msg, 30, TimeUnit.SECONDS);
            this.runner.getAttention(this);
        } catch (InterruptedException e) {
            System.err.println("Unable to queue: " + msg.toString());
            e.printStackTrace();
        }
    }

}
