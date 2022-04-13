package com.lhf.game.battle;

import com.lhf.game.creature.Creature;

import java.util.Collection;

public interface BattleAI {
    Collection<Creature> selectAttackTargets(Collection<Creature> participants);
}
