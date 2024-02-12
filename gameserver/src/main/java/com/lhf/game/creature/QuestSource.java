package com.lhf.game.creature;

import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.TickType;

public class QuestSource extends CreatureEffectSource {
    protected final Deltas onSuccess, onFailure;

    public QuestSource(String name, EffectPersistence persistence, EffectResistance resistance, String description,
            Deltas applicationDeltas, Deltas onSuccess, Deltas onFailure) {
        super(name, persistence, resistance, description, applicationDeltas);
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    public QuestSource(String name, EffectPersistence persistence, EffectResistance resistance, String description,
            Deltas applicationDeltas, Map<TickType, Deltas> tickDeltas, Deltas removalDeltas, Deltas onSuccess,
            Deltas onFailure) {
        super(name, persistence, resistance, description, applicationDeltas, tickDeltas, removalDeltas);
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    @Override
    public QuestSource makeCopy() {
        QuestSource copy = new QuestSource(this.getName(), persistence, resistance, description, onApplication,
                onTickEvent, onRemoval, onSuccess, onFailure);
        return copy;
    }

    public Deltas getOnSuccess() {
        return onSuccess;
    }

    public Deltas getOnFailure() {
        return onFailure;
    }

    @Override
    public boolean isOffensive() {
        return false;
    }

    @Override
    public String printDescription() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("This quest").add(String.format("\"%s\"", this.getName())).add("entails the folowing:");
        sj.add(this.description).add("\r\n");
        if (this.onSuccess != null) {
            final String successDescription = this.onSuccess.printDescription();
            if (successDescription.length() > 0) {
                sj.add("On success:").add(successDescription);
            }
        }
        if (this.onFailure != null) {
            final String failureDescription = this.onFailure.printDescription();
            if (failureDescription.length() > 0) {
                sj.add("On failure:").add(failureDescription);
            }
        }
        if (this.onApplication != null) {
            final String applicationDescription = this.onApplication.printDescription();
            if (applicationDescription.length() > 0) {
                sj.add("On application:").add(applicationDescription);
            }
        }
        if (this.onTickEvent != null && this.onTickEvent.size() > 0) {
            for (final Entry<TickType, Deltas> tickDeltas : this.onTickEvent.entrySet()) {
                final String tickDescription = tickDeltas.getValue().printDescription();
                if (tickDescription.length() > 0) {
                    sj.add("On a").add(tickDeltas.getKey().toString()).add("tick: ").add(tickDescription);
                }
            }
        }
        if (this.onRemoval != null) {
            final String removalDescription = this.onRemoval.printDescription();
            if (removalDescription.length() > 0) {
                sj.add("On removal:").add(removalDescription);
            }
        }
        return sj.toString();
    }

}
