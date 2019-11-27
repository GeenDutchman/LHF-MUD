package com.lhf.game.battle;

import com.lhf.game.creature.Creature;

import java.util.*;

public abstract class BattleAction {
    protected Set<Creature> targets; //this might be better as a `String`??

    public BattleAction() {
        this.targets = new HashSet<>();
    }

    public BattleAction(Creature target) {
        this.targets = new HashSet<>();
        this.targets.add(target);
    }

    public boolean addTarget(Creature target) {
        return this.targets.add(target);
    }

    public boolean addTargets(Collection<Creature> targets) {
        return this.targets.addAll(targets);
    }

    public List<Creature> getTargets() {
        List<Creature> toReturn = new ArrayList<>(this.targets);
        return toReturn;
    }

    public boolean hasTargets() {
        return this.targets.size() > 0;
    }
}
