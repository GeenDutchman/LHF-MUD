package com.lhf.messages.out;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.OutMessageType;

public class CreatureAffectedMessage extends OutMessage {
    private Creature affected;
    private CreatureEffect effect;
    private boolean reversed;

    public CreatureAffectedMessage(Creature affected, CreatureEffect effect) {
        super(OutMessageType.CREATURE_AFFECTED);
        this.affected = affected;
        this.effect = effect;
        this.reversed = false;
    }

    public CreatureAffectedMessage(Creature affected, CreatureEffect effect, boolean reversed) {
        super(OutMessageType.CREATURE_AFFECTED);
        this.affected = affected;
        this.effect = effect;
        this.reversed = reversed;
    }

    public boolean isResultedInDeath() {
        return !this.affected.isAlive();
    }

    public Creature getAffected() {
        return affected;
    }

    public CreatureEffect getEffect() {
        return effect;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.effect.creatureResponsible() != null) {
            sj.add(this.effect.creatureResponsible().getColorTaggedName()).add("used");
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("!");
        } else {
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("affected")
                    .add(this.affected.getColorTaggedName()).add("!");
        }
        sj.add("\r\n");
        if (this.reversed) {
            sj.add("But the effects have EXPIRED, and will now REVERSE!").add("\r\n");
        }
        MultiRollResult damageResults = this.effect.getDamageResult();
        if (damageResults != null) {
            sj.add(this.affected.getColorTaggedName() + "'s").add("health will change by");
            sj.add(damageResults.getColorTaggedName()); // already reversed, if applicable
            sj.add("\r\n");
        }
        if (this.effect.getStatChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Stats, Integer> deltas : this.effect.getStatChanges().entrySet()) {
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
        if (this.effect.getAttributeScoreChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effect.getAttributeScoreChanges().entrySet()) {
                int amount = deltas.getValue();
                if (reversed) {
                    amount *= -1;
                }
                sj.add(deltas.getKey().toString()).add("score will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.effect.getAttributeBonusChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effect.getAttributeBonusChanges().entrySet()) {
                int amount = deltas.getValue();
                if (reversed) {
                    amount *= -1;
                }
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.effect.isRestoreFaction()) {
            sj.add(this.affected.getColorTaggedName() + "'s").add("faction will be restored!");
        }
        return sj.toString();
    }

}
