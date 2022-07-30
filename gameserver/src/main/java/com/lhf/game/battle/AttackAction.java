package com.lhf.game.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lhf.game.creature.Creature;
import com.lhf.game.item.interfaces.Weapon;

public class AttackAction implements BattleAction {

    protected Weapon weapon;
    protected List<Creature> targets;

    public AttackAction(Creature target, Weapon withWeapon) {
        this.weapon = withWeapon;
        this.targets = new ArrayList<>();
        this.addTarget(target);
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public boolean hasWeapon() {
        return this.weapon != null;
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
            sb.append("with ").append(this.weapon.getName());
        }
        return sb.toString();
    }
}
