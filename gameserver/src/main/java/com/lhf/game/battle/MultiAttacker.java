package com.lhf.game.battle;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;

/**
 * Meant to denote Vocations that allow the creature to attack multiple targets
 * in melee
 */
public interface MultiAttacker extends Taggable {
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
    public int maxAttackCount(boolean aggrovate);

    /** Gets the number of targets that the creature can attack at once */
    public boolean canAttackNTargets(int numberOfTargets);

    /** Attacks the number of targets, uses Resources to do so */
    public boolean attackNumberOfTargets(int numberOfTargets);

    /** Gets the number of targets that the creature can aggrovate at once */
    public boolean canAggrovateNTargets(int numberOfTargets);

    /** Aggrovates the number of targets, uses Resources to do so */
    public boolean aggrovateNumberOfTargets(int numberOfTargets);

    /** Gets the Attribute that is keyed to Aggro */
    Attributes getAggrovationAttribute();

    /** Gets how much experience (Vocation Level) goes into making Aggro */
    int getAggrovationLevel();

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
