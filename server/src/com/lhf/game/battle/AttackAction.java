package com.lhf.game.battle;

import com.lhf.game.creature.Creature;

public class AttackAction extends BattleAction {

    protected String weapon; // this might be better as a `Weapon`??

    public AttackAction(Creature target, String withWeapon) {
        super(target);
        this.weapon = withWeapon;
    }

    public String getWeapon() {
        return weapon;
    }

    public boolean hasWeapon() {
        return (weapon != null) && (weapon.length() > 0);
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
