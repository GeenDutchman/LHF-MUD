package com.lhf.game.magic;

import java.util.List;
import java.util.Map;

import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public class CreatureTargetingSpell extends ISpell implements CreatureEffector {
    protected MultiRollResult damageDone;
    protected CasterVsCreatureStrategy strategy;

    protected CreatureTargetingSpell(CreatureTargetingSpellEntry entry) {
        super(entry);
        this.strategy = null;
        this.damageDone = null;
    }

    private CreatureTargetingSpellEntry getTypedEntry() {
        return (CreatureTargetingSpellEntry) this.entry;
    }

    public CasterVsCreatureStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isSingleTarget() {
        return this.getTypedEntry().isSingleTarget();
    }

    public Map<Stats, Integer> getStatChanges() {
        return this.getTypedEntry().getStatChanges();
    }

    public Map<Attributes, Integer> getAttributeScoreChanges() {
        return this.getTypedEntry().getAttributeScoreChanges();
    }

    public Map<Attributes, Integer> getAttributeBonusChanges() {
        return this.getTypedEntry().getAttributeBonusChanges();
    }

    public List<DamageDice> getDamages() {
        return this.getTypedEntry().getDamages();
    }

    public boolean isRestoreFaction() {
        return this.getTypedEntry().isRestoreFaction();
    }

    @Override
    public boolean isOffensive() {
        if (this.getDamages() != null && this.getDamages().size() > 0) {
            for (DamageDice dd : this.getDamages()) {
                if (dd.getFlavor() != DamageFlavor.HEALING) {
                    return true;
                }
            }
        }
        for (Integer delta : this.getStatChanges().values()) {
            if (delta < 0) {
                return true;
            }
        }
        for (Integer delta : this.getAttributeScoreChanges().values()) {
            if (delta < 0) {
                return true;
            }
        }
        for (Integer delta : this.getAttributeBonusChanges().values()) {
            if (delta < 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MultiRollResult getDamageResult() {
        if (this.damageDone == null) {
            for (DamageDice dd : this.getDamages()) {
                if (this.damageDone == null) {
                    this.damageDone = new MultiRollResult(dd.rollDice());
                } else {
                    this.damageDone.addResult(dd.rollDice());
                }
            }
        }
        return this.damageDone;
    }

    @Override
    public void updateDamageResult(MultiRollResult mrr) {
        this.damageDone = mrr;
    }

}
