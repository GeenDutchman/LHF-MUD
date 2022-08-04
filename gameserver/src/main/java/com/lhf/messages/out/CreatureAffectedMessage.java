package com.lhf.messages.out;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.CreatureEffector;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.OutMessageType;

public class CreatureAffectedMessage extends OutMessage {
    private Creature affected;
    private CreatureEffector effects;

    public CreatureAffectedMessage(Creature affected, CreatureEffector effects) {
        super(OutMessageType.CREATURE_AFFECTED);
        this.affected = affected;
        this.effects = effects;
    }

    public Creature getAffected() {
        return affected;
    }

    public CreatureEffector getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(this.effects.getGeneratedBy().getColorTaggedName());
        if (this.effects.getDamages().size() > 0) {
            sj.add("has dealt");
            RollResult sum = null;
            for (RollResult damages : this.effects.getDamages().values()) {
                if (sum == null) {
                    sum = damages;
                } else {
                    sum.combine(damages);
                }
            }
            sj.add(sum.getColorTaggedName()).add("damage to").add(this.affected.getColorTaggedName()).add("\r\n");
        }
        if (this.effects.getAttributeScoreChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effects.getAttributeScoreChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("score will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.effects.getAttributeBonusChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.effects.getAttributeBonusChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.effects.getStatChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Stats, Integer> deltas : this.effects.getStatChanges().entrySet()) {
                sj.add(deltas.getKey().toString()).add("stat will change by").add(deltas.getValue().toString());
            }
            sj.add("\r\n");
        }
        if (this.effects.isRestoreFaction()) {
            sj.add("And will attempt to restore").add(this.affected.getColorTaggedName() + "'s").add("faction");
        }
        return sj.toString();
    }

}
