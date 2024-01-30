package com.lhf.messages.events;

import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.Taggable;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.Stats;
import com.lhf.messages.GameEventType;

public class CreatureAffectedEvent extends GameEvent {
    private final ICreature affected;
    private final CreatureEffect creatureEffect;
    private final Deltas highlightedDelta;
    private final MultiRollResult damages;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature affected;
        private CreatureEffect creatureEffect;
        private Deltas highlightedDelta;
        private MultiRollResult damages;

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

        public CreatureEffect getCreatureEffect() {
            return this.creatureEffect;
        }

        public Builder setEffect(CreatureEffect effect) {
            this.creatureEffect = effect;
            return this;
        }

        public Deltas getHighlightedDelta() {
            return highlightedDelta;
        }

        public Builder setHighlightedDelta(Deltas highlightedDelta) {
            this.highlightedDelta = highlightedDelta;
            return this;
        }

        public MultiRollResult getDamages() {
            return damages;
        }

        public Builder setDamages(MultiRollResult damages) {
            this.damages = damages;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public CreatureAffectedEvent Build() {
            return new CreatureAffectedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public CreatureAffectedEvent(Builder builder) {
        super(builder);
        this.affected = builder.getAffected();
        this.creatureEffect = builder.getCreatureEffect();
        this.highlightedDelta = builder.getHighlightedDelta();
        this.damages = builder.getDamages();
    }

    public boolean isResultedInDeath() {
        return !this.affected.isAlive();
    }

    public ICreature getAffected() {
        return affected;
    }

    protected CreatureEffect getCreatureEffect() {
        return this.creatureEffect;
    }

    public boolean isOffensive() {
        return this.creatureEffect != null ? this.creatureEffect.isOffensive() : false;
    }

    public ICreature getCreatureResponsible() {
        return this.creatureEffect != null ? this.creatureEffect.creatureResponsible() : null;
    }

    public Taggable getGeneratedBy() {
        return this.creatureEffect != null ? this.creatureEffect.getGeneratedBy() : null;
    }

    public Deltas getHighlightedDelta() {
        return highlightedDelta;
    }

    public MultiRollResult getDamages() {
        return damages;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.getCreatureResponsible() != null) {
            sj.add(this.getCreatureResponsible().getColorTaggedName()).add("used");
            sj.add(this.getGeneratedBy().getColorTaggedName()).add("on");
        } else {
            sj.add(this.getGeneratedBy().getColorTaggedName()).add("affected");
        }
        sj.add(this.addressCreature(this.affected, false) + "!");
        sj.add("\r\n");
        MultiRollResult damageResults = this.getDamages();
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
        if (this.highlightedDelta == null) {
            return sj.toString();
        }
        if (this.highlightedDelta.getStatChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Stats, Integer> deltas : this.highlightedDelta.getStatChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("stat will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.isResultedInDeath()) {
            sj.add("And as a result of these things,").add(this.affected.getColorTaggedName()).add("has died.");
            return sj.toString();
        }
        if (this.highlightedDelta.getAttributeScoreChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeScoreChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("score will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.highlightedDelta.getAttributeBonusChanges().size() > 0) {
            sj.add(this.affected.getColorTaggedName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeBonusChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.highlightedDelta.isRestoreFaction()) {
            sj.add(this.affected.getColorTaggedName() + "'s").add("faction will be restored!");
        }
        return sj.toString();
    }

    @Override
    public String print() {
        return this.toString();
    }

}
