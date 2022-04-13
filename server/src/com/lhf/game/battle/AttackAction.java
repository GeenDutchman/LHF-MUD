package com.lhf.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.creature.Creature;

public class AttackAction implements BattleAction {

    protected String weapon; // this might be better as a `Weapon`??
    protected Set<Creature> targets;

    public AttackAction(Creature target, String withWeapon) {
        this.weapon = withWeapon;
        this.targets = new HashSet<>();
        this.addTarget(target);
    }

    public String getWeapon() {
        return weapon;
    }

    public boolean hasWeapon() {
        return (weapon != null) && (weapon.length() > 0);
    }

    @Override
    public AttackAction addTarget(Creature target) {
        this.targets.add(target);
        return this;
    }

    @Override
    public AttackAction addTargets(Collection<Creature> targets) {
        this.targets.addAll(targets);
        return this;
    }

    @Override
    public List<Creature> getTargets() {
        return new ArrayList<Creature>(this.targets);
    }

    @Override
    public boolean hasTargets() {
        return this.targets.size() > 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("attacks target");

        if (targets.size() > 1) {
            sb.append("s ");
        } else {
            sb.append(' ');
        }

        for (Creature c : targets) {
            sb.append(c.getColorTaggedName()).append(' ');
        }
        if (this.hasWeapon()) {
            sb.append("with ").append(this.weapon);
        }
        return sb.toString();
    }
}
