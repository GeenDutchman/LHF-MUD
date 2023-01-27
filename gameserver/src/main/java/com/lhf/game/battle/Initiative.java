package com.lhf.game.battle;

import java.util.Collection;

import com.lhf.game.CreatureContainer;
import com.lhf.game.creature.Creature;

interface Initiative extends CreatureContainer {

    public interface Builder {
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

}