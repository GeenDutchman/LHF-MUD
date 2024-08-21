package com.lhf.game.enums;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;

/**
 * This enum gives distinct steps of Healing, with potency from least to
 * greatest: Regular, Greater, Superior, and Critical.
 */
public enum HealType {
    /**
     * A regular HealType is the basic HealType
     */
    Regular,
    /**
     * A Greater HealType is more potent than a Regular, but still less than
     * Superior
     */
    Greater,
    /**
     * A Superior HealType is the second-strongest, more potent than a Greater, but
     * a step below Critical
     */
    Superior,
    /**
     * A Critical HealType is the most potent of them all, a step above Superior
     */
    Critical;

    public DamageDice produceDamageType() {
        switch (this) {
        case Critical:
            return new DamageDice(3, DieType.EIGHT, DamageFlavor.HEALING);
        case Superior:
            return new DamageDice(3, DieType.SIX, DamageFlavor.HEALING);
        case Greater:
            return new DamageDice(2, DieType.SIX, DamageFlavor.HEALING);
        case Regular:
            // fallthrough
        default:
            return new DamageDice(1, DieType.FOUR, DamageFlavor.HEALING);
        }
    }

    public int produceStaticBonus() {
        return this.ordinal() + 1;
    }
}