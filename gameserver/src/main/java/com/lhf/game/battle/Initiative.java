package com.lhf.game.battle;

import java.util.Collection;

import com.lhf.Examinable;
import com.lhf.game.creature.Creature;
import com.lhf.messages.out.OutMessage;

interface Initiative extends Examinable {

    public Collection<Creature> getParticipants();

    public Creature getCurrent();

    public boolean addCreature(Creature joiner);

    public boolean removeCreature(Creature leaver);

    public default boolean hasCreature(Creature present) {
        Collection<Creature> battlers = this.getParticipants();
        return battlers != null && battlers.contains(present);
    }

    public default int size() {
        Collection<Creature> battlers = this.getParticipants();
        return battlers != null ? battlers.size() : 0;
    }

    public default boolean isRunning() {
        Collection<Creature> battlers = this.getParticipants();
        return battlers != null && battlers.size() > 0;
    }

    public void start();

    public void stop();

    public Creature nextTurn();

    public default boolean announce(OutMessage outMessage) {
        Collection<Creature> battlers = this.getParticipants();
        if (battlers == null || battlers.size() == 0) {
            return false;
        }
        for (Creature battler : battlers) {
            battler.sendMsg(outMessage);
        }
        return true;
    }

}