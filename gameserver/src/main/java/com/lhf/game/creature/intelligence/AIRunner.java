package com.lhf.game.creature.intelligence;

import com.lhf.game.creature.INonPlayerCharacter;

/**
 * This interface is meant to give a thread control of a BasicAI.
 */
public interface AIRunner extends Runnable {

    /**
     * This is meant to limit how many OutMessages to poll from the BasicAI.
     * 
     * If the number is less than or equal to zero, there is no limit except what is
     * stored
     * by the BasicAI. If it is greater than zero, then it will process that many,
     * with
     * respect to how many are stored.
     * 
     * @return chew
     */
    public int getChew();

    /**
     * This is meant to stop the running thread.
     */
    public void stopIt();

    /**
     * Checks to see if it is stopped
     * 
     * @return stopped
     */
    public boolean isStopped();

    /**
     * This will let the ai get the runner's attention
     * 
     * @param ai
     * @throws InterruptedException
     */
    public void getAttention(BasicAI ai) throws InterruptedException;

    /**
     * Registers an NPC with the AIRunner, and as well, will register the variadic
     * handlers
     * with the BasicAI.
     * 
     * @param npc      to register
     * @param handlers variadic argument of handlers to register
     * @return null if cannot be registered, or populated if can be or is already
     *         registered
     */
    public BasicAI register(INonPlayerCharacter npc, AIHandler... handlers);
}
