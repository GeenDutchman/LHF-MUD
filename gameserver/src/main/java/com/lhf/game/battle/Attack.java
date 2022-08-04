package com.lhf.game.battle;

import java.util.Iterator;
import java.util.Map;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.DamageFlavor;

public class Attack extends CreatureEffector implements Iterable<Map.Entry<DamageFlavor, RollResult>> {
    // TODO: add aggro
    private Creature attacker;
    private RollResult toHit;

    public Attack(Creature attacker, RollResult toHit) {
        super(attacker, EffectPersistence.INSTANT);
        this.toHit = toHit;
        this.attacker = attacker;
    }

    public Attack addFlavorAndRoll(DamageFlavor flavor, RollResult attackDamage) {
        super.addDamage(flavor, attackDamage);
        return this;
    }

    // Will first try to add bonus to `flavor` and if that isn't there, will try to
    // add it to any one flavor. Else, no change.
    public Attack addDamageBonus(DamageFlavor flavor, int bonus) {
        if (this.getDamages().containsKey(flavor)) {
            this.getDamages().put(flavor, this.getDamages().get(flavor).addBonus(bonus));
        } else if (this.getDamages().size() > 0) {
            for (DamageFlavor iterFlavor : this.getDamages().keySet()) {
                this.getDamages().put(iterFlavor, this.getDamages().get(iterFlavor).addBonus(bonus));
                break;
            }
        }
        return this;
    }

    public Attack addToHitBonus(int bonus) {
        this.toHit.addBonus(bonus);
        return this;
    }

    public RollResult getToHit() {
        return toHit;
    }

    public Creature getAttacker() {
        return attacker;
    }

    public Attack setAttacker(Creature attacker) {
        this.attacker = attacker;
        return this;
    }

    @Override
    public Iterator<Map.Entry<DamageFlavor, RollResult>> iterator() {
        return this.getDamages().entrySet().iterator();
    }
}
