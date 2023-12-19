package com.lhf.game;

import java.util.EnumSet;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DiceDC;
import com.lhf.game.dice.MultiRollResult;
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
    private final EnumSet<Attributes> actorAttrs;
    private final Stats actorStat;
    private final Integer actorDC;

    // these are for the target of the effect
    private final EnumSet<Attributes> targetAttrs;
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
        this.actorAttrs = EnumSet.of(actorAttr);
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttrs = null;
        this.targetStat = targetStat;
        this.targetDC = null;
        this.resistAmount = TargetResistAmount.ALL;
    }

    /**
     * This is used to pit an actor's highest modded attribute check against a
     * target's stat,
     * usually a melee attack like a STR/DEX check vs the target's AC.
     * 
     * @param actorAttrs
     * @param targetStat
     */
    public EffectResistance(EnumSet<Attributes> actorAttrs, Stats targetStat) {
        this.actorAttrs = actorAttrs;
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttrs = null;
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
        this.actorAttrs = EnumSet.of(actorAttr);
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttrs = EnumSet.of(targetAttr);
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
        this.actorAttrs = EnumSet.of(actorAttr);
        this.actorStat = null;
        this.actorDC = null;
        this.targetAttrs = null;
        this.targetStat = null;
        this.targetDC = targetDC;
        this.resistAmount = resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    /**
     * Used by builders to define everything.
     * 
     * @param actorAttrs
     * @param actorStat
     * @param actorDC
     * @param targetAttrs
     * @param targetStat
     * @param targetDC
     * @param resistAmount defaults to resisting all
     */
    public EffectResistance(EnumSet<Attributes> actorAttrs, Stats actorStat, Integer actorDC,
            EnumSet<Attributes> targetAttrs, Stats targetStat, Integer targetDC, TargetResistAmount resistAmount) {
        this.actorAttrs = actorAttrs;
        this.actorStat = actorStat;
        this.actorDC = actorDC;
        this.targetAttrs = targetAttrs;
        this.targetStat = targetStat;
        this.targetDC = targetDC;
        this.resistAmount = resistAmount != null ? resistAmount : TargetResistAmount.ALL;
    }

    public Stats getActorStat() {
        return actorStat;
    }

    public EnumSet<Attributes> getActorAttrs() {
        return actorAttrs;
    }

    public EnumSet<Attributes> getTargetAttrs() {
        return targetAttrs;
    }

    public Integer getActorDC() {
        return actorDC;
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
        if (actorAttrs != null && actorAttrs.size() > 0) {
            StringJoiner sj = new StringJoiner(", ");
            for (Attributes attr : actorAttrs) {
                sj.add(attr.name());
            }
            if (actorAttrs.size() > 1) {
                sb.append("The causing party's check using the highest modifier between ").append(sj.toString())
                        .append(" ");
            } else {
                sb.append("The causing party's ").append(sj.toString()).append(" check ");
            }
        } else if (actorStat != null) {
            sb.append("The causing party's ").append(actorStat).append(" stat ");
        } else if (actorDC != null) {
            sb.append("The static DC of ").append(actorDC).append(" ");
        } else {
            return " The target automatically succeeds. ";
        }
        if (targetAttrs != null && targetAttrs.size() > 0) {
            StringJoiner sj = new StringJoiner(", ");
            for (Attributes attr : actorAttrs) {
                sj.add(attr.name());
            }
            sb.append("v.s. ");
            if (actorAttrs.size() > 1) {
                sb.append("the target's check using the highest modifier between ").append(sj.toString())
                        .append(". ");
            } else {
                sb.append("the target's ").append(sj.toString()).append(" check. ");
            }
            sb.append("v.s. the target's ").append(targetAttrs).append(" check. ");
        } else if (targetStat != null) {
            sb.append("v.s. the target's ").append(targetStat).append(" stat. ");
        } else if (targetDC != null) {
            sb.append("v.s. the target DC of ").append(targetDC).append(". ");
        } else {
            return " The effect is automatically applied. ";
        }
        return sb.toString();
    }

    public MultiRollResult actorEffort(ICreature actor, int bonus) {
        MultiRollResult.Builder result = new MultiRollResult.Builder();
        if (actor != null && actorAttrs != null && actorAttrs.size() > 0) {
            Attributes highest = actor.getHighestAttributeBonus(actorAttrs);
            if (highest == null) {
                return null;
            }
            result.addMultiRollResult(actor.check(highest));
        } else if (actor != null && actorStat != null) {
            result.addRollResults((new DiceDC(actor.getStats().getOrDefault(actorStat, 1)).rollDice()));
        } else if (actorDC != null) {
            result.addRollResults((new DiceDC(actorDC).rollDice()));
        }
        if (result != null && bonus != 0) {
            result.addBonuses(bonus);
        }
        return result.Build();
    }

    public MultiRollResult targetEffort(ICreature target, int bonus) {
        MultiRollResult.Builder result = new MultiRollResult.Builder();
        if (target != null && targetAttrs != null && targetAttrs.size() > 0) {
            Attributes highest = target.getHighestAttributeBonus(targetAttrs);
            if (highest == null) {
                return null;
            }
            result.addMultiRollResult(target.check(highest));
        } else if (target != null && targetStat != null) {
            result.addRollResults(new DiceDC(target.getStats().getOrDefault(targetStat, 1)).rollDice());
        } else if (targetDC != null) {
            result.addRollResults(new DiceDC(targetDC).rollDice());
        }
        if (result != null && bonus != 0) {
            result.addBonuses(bonus);
        }
        return result.Build();
    }

    public MultiRollResult targetEffort(int bonus) {
        return this.targetEffort(null, bonus);
    }

}
