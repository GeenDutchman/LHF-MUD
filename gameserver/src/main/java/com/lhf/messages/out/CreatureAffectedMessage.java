package com.lhf.messages.out;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.OutMessageType;

public class CreatureAffectedMessage extends OutMessage {
    private Creature affected;
    private CreatureEffector effects;
    private boolean reversed;

    public CreatureAffectedMessage(Creature affected, CreatureEffector effects) {
        super(OutMessageType.CREATURE_AFFECTED);
        this.affected = affected;
        this.effects = effects;
        this.reversed = false;
    }

    public CreatureAffectedMessage(Creature affected, CreatureEffector effects, boolean reversed) {
        super(OutMessageType.CREATURE_AFFECTED);
        this.affected = affected;
        this.effects = effects;
        this.reversed = reversed;
    }

    public boolean isResultedInDeath() {
        return !this.affected.isAlive();
    }

    public Creature getAffected() {
        return affected;
    }

    public CreatureEffector getEffects() {
        return effects;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.effects.creatureResponsible() != null) {
            sj.add(this.effects.creatureResponsible().getColorTaggedName()).add("used");
            sj.add(this.effects.getGeneratedBy().getColorTaggedName()).add("!");
        } else {
            sj.add(this.effects.getGeneratedBy().getColorTaggedName()).add("affected")
                    .add(this.affected.getColorTaggedName()).add("!");
        }
        sj.add("\r\n");
        if (this.reversed) {
            sj.add("But the effects have EXPIRED, and will now REVERSE!").add("\r\n");
        }
        MultiRollResult damageResults = this.effects.getDamageResult();
        if (damageResults != null) {
            sj.add(this.affected.getColorTaggedName() + "'s").add("health will change by");
            sj.add(damageResults.getColorTaggedName()); // already reversed, if applicable
            sj.add("\r\n");
        }
        if (this.effects.getStatChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Stats, Integer> deltas : this.effects.getStatChanges().entrySet()) {
                int amount = deltas.getValue();
                if (reversed) {
                    amount *= -1;
                }
                sj.add(deltas.getKey().toString()).add("stat will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.isResultedInDeath()) {
            sj.add("And as a result of these things,").add(this.affected.getColorTaggedName()).add("has died.");
            return sj.toString();
        }
        if (this.effects.getAttributeScoreChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effects.getAttributeScoreChanges().entrySet()) {
                int amount = deltas.getValue();
                if (reversed) {
                    amount *= -1;
                }
                sj.add(deltas.getKey().toString()).add("score will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.effects.getAttributeBonusChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effects.getAttributeBonusChanges().entrySet()) {
                int amount = deltas.getValue();
                if (reversed) {
                    amount *= -1;
                }
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.effects.isRestoreFaction()) {
            sj.add(this.affected.getColorTaggedName() + "'s").add("faction will be restored!");
        }
        return sj.toString();
    }

}
