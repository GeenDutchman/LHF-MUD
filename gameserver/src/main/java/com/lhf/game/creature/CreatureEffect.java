package com.lhf.game.creature;

import java.util.EnumMap;
import java.util.Map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.MultiRollResult;

public class CreatureEffect extends EntityEffect {
    protected MultiRollResult applicationDamageResult, removalDamageResult;
    protected Map<TickType, MultiRollResult> tickDamageResult;

    public CreatureEffect(CreatureEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.applicationDamageResult = null;
        this.removalDamageResult = null;
        this.tickDamageResult = new EnumMap<>(TickType.class);
    }

    public CreatureEffectSource getSource() {
        return (CreatureEffectSource) this.source;
    }

    public void updateApplicationDamage(MultiRollResult mrr) {
        this.applicationDamageResult = mrr;
    }

    public void updateRemovalDamage(MultiRollResult mrr) {
        this.removalDamageResult = mrr;
    }

    public void updateTickDamge(TickType tt, MultiRollResult mrr) {
        this.tickDamageResult.put(tt, mrr);
    }

    public MultiRollResult getApplicationDamageResult() {
        return applicationDamageResult;
    }

    public MultiRollResult getRemovalDamageResult() {
        return removalDamageResult;
    }

    public Map<TickType, MultiRollResult> getTickDamageResult() {
        return tickDamageResult;
    }

    public Deltas getApplicationDeltas() {
        return this.getSource().getOnApplication();
    }

    public Deltas getOnRemovalDeltas() {
        return this.getSource().getOnRemoval();
    }

    public Deltas getDeltasForTick(TickType tickType) {
        return this.getSource().deltasForTick(tickType);
    }

    public boolean isOffensive() {
        return this.getSource().isOffensive();
    }

}
