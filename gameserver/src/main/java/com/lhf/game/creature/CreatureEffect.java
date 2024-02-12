package com.lhf.game.creature;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.events.GameEvent;

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
        if (applicationDamageResult == null) {
            final Deltas deltas = this.getSource().getOnApplication();
            if (deltas != null) {
                applicationDamageResult = deltas.rollDamages();
            }
        }
        return applicationDamageResult;
    }

    public MultiRollResult getRemovalDamageResult() {
        if (removalDamageResult == null) {
            final Deltas deltas = this.getSource().getOnRemoval();
            if (deltas != null) {
                removalDamageResult = deltas.rollDamages();
            }
        }
        return removalDamageResult;
    }

    public MultiRollResult getTickDamageResult(TickType tickType) {
        return tickDamageResult.computeIfAbsent(tickType, (tt) -> {
            final Map<TickType, Deltas> deltasMap = this.getSource().getOnTickEvent();
            if (deltasMap == null) {
                return null;
            }
            final Deltas deltas = deltasMap.getOrDefault(tt, null);
            if (deltas == null) {
                return null;
            }
            return deltas.rollDamages();
        });
    }

    public Map<TickType, MultiRollResult> getTickDamageResult() {
        return Collections.unmodifiableMap(tickDamageResult);
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

    public Deltas getDeltasForEvent(GameEvent event) {
        if (event == null) {
            return null;
        }
        final TickType tickType = event.getTickType();
        return this.getDeltasForTick(tickType);
    }

    public boolean isOffensive() {
        return this.getSource().isOffensive();
    }

}
