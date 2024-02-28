package com.lhf.game.creature;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;

import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.GameEventTester;

public class CreatureEffect extends EntityEffect {
    protected MultiRollResult applicationDamageResult, removalDamageResult;
    protected Map<GameEventTester, MultiRollResult> tickDamageResult;

    public CreatureEffect(CreatureEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        super(source, creatureResponsible, generatedBy);
        this.applicationDamageResult = null;
        this.removalDamageResult = null;
        this.tickDamageResult = new TreeMap<>();
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

    public MultiRollResult getEventDamageResult(GameEvent event,
            Function<MultiRollResult, MultiRollResult> adjustor) {
        if (event == null) {
            return null;
        }
        final Entry<GameEventTester, Deltas> entry = this.getSource().getTesterEntryForEvent(event);
        if (entry == null) {
            return null;
        }
        final GameEventTester tester = entry.getKey();
        final Deltas deltas = entry.getValue();
        if (tester == null || deltas == null) {
            return null;
        }
        return tickDamageResult.compute(tester, (tt, value) -> {
            if (value != null) {
                return adjustor != null ? adjustor.apply(value) : value;
            }

            return adjustor != null ? adjustor.apply(deltas.rollDamages()) : deltas.rollDamages();
        });
    }

    public Map<GameEventTester, MultiRollResult> getTickDamageResult() {
        return Collections.unmodifiableMap(tickDamageResult);
    }

    public Deltas getApplicationDeltas() {
        return this.getSource().getOnApplication();
    }

    public Deltas getOnRemovalDeltas() {
        return this.getSource().getOnRemoval();
    }

    public Deltas getDeltasForEvent(GameEvent event) {
        if (event == null) {
            return null;
        }
        return this.getSource().getDeltasForEvent(event);
    }

    public boolean isOffensive() {
        return this.getSource().isOffensive();
    }

}
