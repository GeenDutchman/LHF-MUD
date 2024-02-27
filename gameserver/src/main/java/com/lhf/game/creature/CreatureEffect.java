package com.lhf.game.creature;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

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

    public MultiRollResult getApplicationDamageResult(Function<MultiRollResult, MultiRollResult> adjustor) {
        if (applicationDamageResult == null) {
            final Deltas deltas = this.getSource().getOnApplication();
            if (deltas != null) {
                applicationDamageResult = adjustor != null ? adjustor.apply(deltas.rollDamages())
                        : deltas.rollDamages();
            }
        } else if (adjustor != null) {
            this.applicationDamageResult = adjustor.apply(this.applicationDamageResult);
        }
        return applicationDamageResult;
    }

    public MultiRollResult getRemovalDamageResult(Function<MultiRollResult, MultiRollResult> adjustor) {
        if (removalDamageResult == null) {
            final Deltas deltas = this.getSource().getOnRemoval();
            if (deltas != null) {
                removalDamageResult = adjustor != null ? adjustor.apply(deltas.rollDamages()) : deltas.rollDamages();
            }
        } else if (adjustor != null) {
            this.removalDamageResult = adjustor.apply(this.removalDamageResult);
        }
        return removalDamageResult;
    }

    public MultiRollResult getTickDamageResult(TickType tickType, Function<MultiRollResult, MultiRollResult> adjustor) {
        if (tickType == null) {
            return null;
        }
        return tickDamageResult.compute(tickType, (tt, value) -> {
            if (value != null) {
                return adjustor != null ? adjustor.apply(value) : value;
            }
            final Map<TickType, Deltas> deltasMap = this.getSource().getOnTickEvent();
            if (deltasMap == null) {
                return null;
            }
            final Deltas deltas = deltasMap.getOrDefault(tt, null);
            if (deltas == null) {
                return null;
            }
            return adjustor != null ? adjustor.apply(deltas.rollDamages()) : deltas.rollDamages();
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
