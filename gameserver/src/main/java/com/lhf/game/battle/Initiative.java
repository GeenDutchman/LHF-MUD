package com.lhf.game.battle;

import java.util.Collection;

import com.lhf.game.CreatureContainer;
import com.lhf.game.TickType;
import com.lhf.game.creature.Creature;
import com.lhf.game.events.messages.out.TickMessage;

public interface Initiative extends CreatureContainer {

    public interface Builder {
        public abstract boolean addCreature(Creature joiner);

        public abstract Initiative Build();
    }

    public Creature getCurrent();

    public default int size() {
        Collection<Creature> battlers = this.getCreatures();
        return battlers != null ? battlers.size() : 0;
    }

    public default boolean isRunning() {
        Collection<Creature> battlers = this.getCreatures();
        return battlers != null && battlers.size() > 0;
    }

    public void start();

    public void stop();

    public Creature nextTurn();

    public default void onTurnStart(Creature starter) {
        // doesn't do much yet
    }

    public default void onTurnEnd(Creature ender) {
        if (ender != null) {
            ender.sendMsg(TickMessage.getBuilder().setNotBroadcast().setTickType(TickType.TURN).Build());
        }
    }

    public default void onRoundEnd() {
        this.announce(TickMessage.getBuilder().setBroacast().setTickType(TickType.ROUND).Build());
    }

    public int getRoundCount();

    public int getTurnCount();

}