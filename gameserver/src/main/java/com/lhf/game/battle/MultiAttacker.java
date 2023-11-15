package com.lhf.game.battle;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.vocation.Fighter.Stamina;
import com.lhf.game.creature.vocation.resourcepools.ResourcePool;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.ResourceCost;

/**
 * Meant to denote Vocations that allow the creature to attack multiple targets
 * in melee
 */
public interface MultiAttacker extends Taggable {
    /** The maximum amount of targets that can be targeted at once */
    final static ResourceCost MAX_COST = ResourceCost.TENTH_MAGNITUDE;
    final static int MAX_TARGETS = 10;
    final static int AGGROVATION_COST = 2;
    final static int EXTRA_TARGET_COST = 1;

    /** Gets the name of the creature who can attack multiple targets in melee */
    public String getName();

    /**
     * Gets the name of the vocation of the creature who can attack multiple targets
     * in melee
     */
    public String getMultiAttackerVocation();

    /**
     * Gets the max count that the creature can attack, possibly with aggrovation
     */
    public default int maxAttackCount(boolean aggrovate) {
        ResourcePool pool = this.getResourcePool();
        if (pool == null) {
            return 1;
        }
        int maxTargets = 1;
        // loop over total targets
        for (int i = 1; i <= MAX_TARGETS; i++) {
            if (this.canAttackNTargets(i, aggrovate)) {
                maxTargets = i;
            }
        }
        return maxTargets;
    }

    default int getExtraTargetCost() {
        return EXTRA_TARGET_COST;
    }

    default int getAggrovationCost() {
        return AGGROVATION_COST;
    }

    default int calculateCost(int numberOfTargets, boolean aggrovate) {
        if (numberOfTargets <= 1 && !aggrovate) {
            return 0;
        }
        int cost = 0;
        if (numberOfTargets > 1) {
            cost += (numberOfTargets - 1) * this.getExtraTargetCost();
        }
        if (aggrovate) {
            cost += numberOfTargets * this.getAggrovationCost();
        }
        return cost;
    }

    /** Gets the number of targets that the creature can attack at once */
    public default boolean canAttackNTargets(int numberOfTargets, boolean aggrovate) {
        ResourcePool pool = this.getResourcePool();
        if (pool == null) {
            return numberOfTargets <= 1 && aggrovate == false;
        }
        int cost = this.calculateCost(numberOfTargets, aggrovate);
        if (cost > MAX_COST.toInt()) {
            return false;
        }
        return pool.checkCost(ResourceCost.fromInt(cost));
    }

    /** Attacks the number of targets, uses Resources to do so */
    public default boolean attackNumberOfTargets(int numberOfTargets, boolean aggrovate) {
        if (!this.canAttackNTargets(numberOfTargets, aggrovate)) {
            return false;
        }
        ResourcePool pool = this.getResourcePool();
        if (pool == null) {
            return false;
        }
        int cost = this.calculateCost(numberOfTargets, aggrovate);
        ResourceCost toPay = ResourceCost.fromInt(cost);
        return pool.payCost(toPay) == toPay;
    }

    /** Gets the Attribute that is keyed to Aggro */
    Attributes getAggrovationAttribute();

    /** Gets how much experience (Vocation Level) goes into making Aggro */
    int getAggrovationLevel();

    /** Gets the resource pool */
    ResourcePool getResourcePool();

    /** Modifies an attack */
    public default Attack modifyAttack(Attack attack, boolean extraAggro) {
        Creature attacker = attack.getAttacker();
        if (attacker != null) {
            int chaMod = Integer.max(attacker.getAttributes().getMod(this.getAggrovationAttribute()), 1);
            if (extraAggro) {
                chaMod += Integer.max(this.getAggrovationLevel(), 1);
            }

            DamageDice dd = new DamageDice(chaMod, DieType.SIX, DamageFlavor.AGGRO);
            for (CreatureEffect crEffect : attack) {
                if (crEffect.isOffensive()) {
                    MultiRollResult mrr = crEffect.getDamageResult();
                    MultiRollResult.Builder rebuilder = new MultiRollResult.Builder().addMultiRollResult(mrr);
                    rebuilder.addRollResults(dd.rollDice());
                    crEffect.updateDamageResult(rebuilder.Build());
                }
            }
        }
        return attack;
    }
}
