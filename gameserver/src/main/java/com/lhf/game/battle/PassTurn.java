package com.lhf.game.battle;

import java.util.Collection;
import java.util.List;

import com.lhf.game.creature.Creature;

public class PassTurn implements BattleAction {

    private Creature passer;

    public PassTurn(Creature passer) {
        this.passer = passer;
    }

    @Override
    public BattleAction addTarget(Creature target) {
        return null;
    }

    @Override
    public BattleAction addTargets(Collection<Creature> targets) {
        return null;
    }

    @Override
    public List<Creature> getTargets() {
        return List.of(this.passer);
    }

    @Override
    public boolean hasTargets() {
        return true;
    }

}
