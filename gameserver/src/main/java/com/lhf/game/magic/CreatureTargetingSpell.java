package com.lhf.game.magic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.strategies.CasterVsCreatureStrategy;

public class CreatureTargetingSpell extends ISpell implements CreatureEffector {
    protected MultiRollResult damageDone;
    protected Optional<CasterVsCreatureStrategy> strategy;

    protected CreatureTargetingSpell(CreatureTargetingSpellEntry entry) {
        super(entry);
        this.strategy = Optional.empty();
        this.damageDone = null;
    }

    private CreatureTargetingSpellEntry getTypedEntry() {
        return (CreatureTargetingSpellEntry) this.entry;
    }

    public Optional<CasterVsCreatureStrategy> getStrategy() {
        return this.strategy;
    }

    public void setStrategy(CasterVsCreatureStrategy strategem) {
        this.strategy = Optional.of(strategem);
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
