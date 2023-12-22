package com.lhf.messages.out;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.GameEventType;

public class CreatureAffectedMessage extends OutMessage {
    private final ICreature affected;
    private final CreatureEffect effect;
    private final boolean reversed;

    public static class Builder extends OutMessage.Builder<Builder> {
        private ICreature affected;
        private CreatureEffect effect;
        private boolean reversed;

        protected Builder() {
            super(GameEventType.CREATURE_AFFECTED);
        }

        public ICreature getAffected() {
            return affected;
        }

        public Builder setAffected(ICreature affected) {
            this.affected = affected;
            return this;
        }

        public CreatureEffect getEffect() {
            return effect;
        }

        public Builder setEffect(CreatureEffect effect) {
            this.effect = effect;
            return this;
        }

        public boolean isReversed() {
            return reversed;
        }

        public Builder setReversed(boolean reversed) {
            this.reversed = reversed;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureAffectedMessage Build() {
            return new CreatureAffectedMessage(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureAffectedMessage(Builder builder) {
        super(builder);
        this.affected = builder.getAffected();
        this.effect = builder.getEffect();
        this.reversed = builder.isReversed();
    }

    public boolean isResultedInDeath() {
        return !this.affected.isAlive();
    }

    public ICreature getAffected() {
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
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("on");
        } else {
            sj.add(this.effect.getGeneratedBy().getColorTaggedName()).add("affected");
        }
        sj.add(this.addressCreature(this.affected, false) + "!");
        sj.add("\r\n");
        if (this.reversed) {
            sj.add("But the effects have EXPIRED, and will now REVERSE!").add("\r\n");
        }
        MultiRollResult damageResults = this.effect.getDamageResult();
        if (damageResults != null && !damageResults.isEmpty()) {
            if (!this.isBroadcast()) {
                sj.add("Your");
            } else if (this.affected != null) {
                sj.add(this.affected.getColorTaggedName() + "'s");
            } else {
                sj.add("Their");
            }
            sj.add("health will change by");
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

    @Override
    public String print() {
        return this.toString();
    }

}
