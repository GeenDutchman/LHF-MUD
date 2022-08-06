package com.lhf.game.battle;

import com.lhf.game.creature.Creature;

import java.util.*;

public interface BattleAction {
    // TODO: get rid of this

    public BattleAction addTarget(Creature target);

    public BattleAction addTargets(Collection<Creature> targets);

    public List<Creature> getTargets();

    public boolean hasTargets();
}
