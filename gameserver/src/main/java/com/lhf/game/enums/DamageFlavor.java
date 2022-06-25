package com.lhf.game.enums;

import com.lhf.Taggable;

public enum DamageFlavor implements Taggable {
    SLASHING, MAGICAL_SLASHING, BLUDGEONING, MAGICAL_BLUDGEONING, PIERCING, MAGICAL_PIERCING,
    VOID, FORCE, FIRE, COLD, HEALING, NECROTIC, POISON, ACID, LIGHTNING, THUNDER, PSYCHIC;

    @Override
    public String getStartTag() {
        return "<dmgFlavor>";
    }

    @Override
    public String getEndTag() {
        return "</dmgFlavor>";
    }

    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }

}
