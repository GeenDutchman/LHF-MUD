package com.lhf.game;

import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;

public class EffectResistance {
    /**
     * This is to specify by how much a target can resist an effect with
     * numeric consequences.
     * Note that it has nothing to do with a Creature's innate `DamageFlavor`
     * adjustments.
     */
    public enum TargetResistAmount {
        HALF, ALL;
    }

    // These are for the doer of the effect
    private final Attributes actorAttr;
    private final Stats actorStat;
    private final Integer actorDC;

    // these are for the target of the effect
    private final Attributes targetAttr;
    private final Stats targetStat;
    private final Integer targetDC;

    private final TargetResistAmount resistAmount;

    /**
     * This is used to pit an actor's attribute check against a target's stat,
     * usually a melee attack like a STR check vs the target's AC.
     * 
     * @param actorAttr
     * @param targetStat
     */
    public EffectResistance(Attributes actorAttr, Stats targetStat) {
        this.actorAttr = actorAttr;
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttr = null;
        this.targetStat = targetStat;
        this.targetDC = null;
        this.resistAmount = TargetResistAmount.ALL;
    }

    /**
     * This is used to pit an actor's attribute check against a target's attribute
     * check,
     * usually for contested rolls of some kind.
     * The attributes do not have to be the same.
     * It can be specified by how much the target can resist.
     * 
     * @param actorAttr
     * @param targetAttr
     * @param resistAmount defaults to resisting all
     */
    public EffectResistance(Attributes actorAttr, Attributes targetAttr, TargetResistAmount resistAmount) {
        this.actorAttr = actorAttr;
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttr = targetAttr;
        this.targetStat = null;
        this.targetDC = null;
        this.resistAmount = resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    /**
     * This is used to pit an actor's attribute check against a static DC,
     * usually a check against the Room or something to achieve an effect.
     * The resist amount may or may not factor into that.
     * 
     * @param actorAttr
     * @param targetDC
     * @param resistAmount defaults to resisting all
     */
    public EffectResistance(Attributes actorAttr, Integer targetDC, TargetResistAmount resistAmount) {
        this.actorAttr = actorAttr;
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttr = null;
        this.targetStat = null;
        this.targetDC = targetDC;
        this.resistAmount = resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    /**
     * Used by Builders to specify everything about the resistance.
     * 
     * @param actorAttr
     * @param actorStat
     * @param actorDC
     * @param targetAttr
     * @param targetStat
     * @param targetDC
     * @param resistAmount defaults to resisting all
     */
    public EffectResistance(Attributes actorAttr, Stats actorStat, Integer actorDC, Attributes targetAttr,
            Stats targetStat, Integer targetDC, TargetResistAmount resistAmount) {
        this.actorAttr = actorAttr;
        this.actorStat = actorStat;
        this.actorDC = actorDC;
        this.targetAttr = targetAttr;
        this.targetStat = targetStat;
        this.targetDC = targetDC;
        this.resistAmount = resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    public Attributes getActorAttr() {
        return actorAttr;
    }

    public Stats getActorStat() {
        return actorStat;
    }

    public Integer getActorDC() {
        return actorDC;
    }

    public Attributes getTargetAttr() {
        return targetAttr;
    }

    public Stats getTargetStat() {
        return targetStat;
    }

    public Integer getTargetDC() {
        return targetDC;
    }

    public TargetResistAmount getResistAmount() {
        return resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Resistance to this effect is determined by: ");
        if (actorAttr != null) {
            sb.append("The causing party's ").append(actorAttr).append(" check ");
        } else if (actorStat != null) {
            sb.append("The causing party's ").append(actorStat).append(" stat ");
        } else if (actorDC != null) {
            sb.append("The static DC of ").append(actorDC).append(" ");
        } else {
            return " The target automatically succeeds. ";
        }
        if (targetAttr != null) {
            sb.append("v.s. the target's ").append(targetAttr).append(" check. ");
        } else if (targetStat != null) {
            sb.append("v.s. the target's ").append(targetStat).append(" stat. ");
        } else if (targetDC != null) {
            sb.append("v.s. the target DC of ").append(targetDC).append(". ");
        } else {
            return " The effect is automatically applied. ";
        }
        return sb.toString();
    }
}
