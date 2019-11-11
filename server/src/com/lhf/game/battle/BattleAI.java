package com.lhf.game.battle;

import com.lhf.game.creature.Creature;

import java.util.Collection;

public interface BattleAI {
    String performBattleTurn(Collection<Creature> participants);
}
