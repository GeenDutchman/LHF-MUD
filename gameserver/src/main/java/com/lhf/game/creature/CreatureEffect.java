package com.lhf.game.creature;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;

public class CreatureEffect extends EntityEffect {
    protected MultiRollResult damageResult;

    public CreatureEffect(CreatureEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.damageResult = null;
    }

    public CreatureEffectSource getSource() {
        return (CreatureEffectSource) this.source;
    }

    public MultiRollResult getDamageResult() {
        if (this.damageResult == null) {
            MultiRollResult.Builder mrrBuilder = new MultiRollResult.Builder();
            for (DamageDice dd : this.getSource().getDamages()) {
                mrrBuilder.addRollResults(dd.rollDice());
            }
            this.damageResult = mrrBuilder.Build();
        }
        return this.damageResult;
    }

    public CreatureEffect addDamageBonus(int bonus) {
        if (this.getDamageResult() != null) {
            this.damageResult = new MultiRollResult.Builder()
                    .addMultiRollResult(this.getDamageResult())
                    .addBonuses(bonus).Build();
        }
        return this;
    }

    public void updateDamageResult(MultiRollResult mrr) {
        this.damageResult = mrr;
    }

    public Map<Stats, Integer> getStatChanges() {
        return Collections.unmodifiableMap(this.getSource().getStatChanges());
    }

    public Map<Attributes, Integer> getAttributeScoreChanges() {
        return Collections.unmodifiableMap(this.getSource().getAttributeScoreChanges());
    }

    public Map<Attributes, Integer> getAttributeBonusChanges() {
        return Collections.unmodifiableMap(this.getSource().getAttributeBonusChanges());
    }

    public List<DamageDice> getDamages() {
        return Collections.unmodifiableList(this.getSource().getDamages());
    }

    public boolean isRestoreFaction() {
        return this.getSource().isRestoreFaction();
    }

    public boolean isOffensive() {
        return this.getSource().isOffensive();
    }

}
