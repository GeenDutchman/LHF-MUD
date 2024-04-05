package com.lhf.messages.events;

import java.util.EnumSet;
import java.util.Map;

import com.lhf.OutputBuilder;
import com.lhf.Taggable;
import com.lhf.Taggable.BasicTaggable;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.Stats;
import com.lhf.messages.GameEventType;

public class CreatureAffectedEvent extends GameEvent {
    private final ICreature affected;
    private final transient ICreature creatureResponsible;
    private final BasicTaggable generatedBy;
    private final Deltas highlightedDelta;
    private final MultiRollResult damages;

    public static class Builder extends GameEvent.Builder<Builder> {
        private ICreature affected;
        private ICreature creatureResponsible;
        private Taggable generatedBy;
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

        /**
         * Pulls data from the effect, defaults to application deltas
         * 
         * @deprecated Prefer the piecemeal {@link #setCreatureResponsible(ICreature)},
         *             {@link #setGeneratedBy(Taggable)},
         *             {@link #setDamages(MultiRollResult)} and
         *             {@link #setHighlightedDelta(Deltas)}
         * 
         * @param effect
         * @return
         */
        @Deprecated
        public Builder fromCreatureEffect(CreatureEffect effect) {
            if (effect != null) {
                this.setCreatureResponsible(effect.creatureResponsible()).setGeneratedBy(effect.getGeneratedBy());
            }
            return this;
        }

        public ICreature getCreatureResponsible() {
            return creatureResponsible;
        }

        public Builder setCreatureResponsible(ICreature creatureResponsible) {
            this.creatureResponsible = creatureResponsible;
            return this;
        }

        public BasicTaggable getGeneratedBy() {
            return Taggable.basicTaggable(this.generatedBy);
        }

        public Builder setGeneratedBy(Taggable generatedBy) {
            this.generatedBy = generatedBy;
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
            return damages != null ? damages
                    : (this.highlightedDelta != null ? this.highlightedDelta.rollDamages() : this.damages);
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
        this.creatureResponsible = builder.getCreatureResponsible();
        this.generatedBy = builder.getGeneratedBy();
        this.highlightedDelta = builder.getHighlightedDelta();
        this.damages = builder.getDamages();
    }

    public boolean isResultedInDeath() {
        return !this.affected.isAlive();
    }

    public ICreature getAffected() {
        return affected;
    }

    public boolean isOffensive() {
        if (this.highlightedDelta != null) {
            return this.highlightedDelta.isOffensive();
        }
        if (this.damages != null) {
            EnumSet<DamageFlavor> offenseSet = EnumSet.allOf(DamageFlavor.class);
            offenseSet.remove(DamageFlavor.HEALING);
            int offenseTotal = this.damages.getByFlavors(offenseSet, true);
            if (offenseTotal != 0) {
                return true;
            }
        }
        return false;
    }

    public ICreature getCreatureResponsible() {
        return this.creatureResponsible;
    }

    public Taggable getGeneratedBy() {
        return this.generatedBy;
    }

    public Deltas getHighlightedDelta() {
        return highlightedDelta;
    }

    public MultiRollResult getDamages() {
        return damages;
    }

    @Override
    public String toString() {
        return this.printString();
    }

    @Override
    public void buildOutput(OutputBuilder builder) {
        if (builder == null) {
            return;
        }
        OutputBuilder affectation = builder.produceSubBuilder("Affectation");
        if (this.creatureResponsible != null) {
            affectation.appendTaggable(this.creatureResponsible);
            if (this.generatedBy != null) {
                affectation.appendString("used");
                affectation.appendTaggable(this.generatedBy);
                affectation.appendString("on");
            } else {
                affectation.appendString("affected");
            }
            this.addressCreature(affectation, this.affected, false);
            affectation.appendString("!", null, null);
        } else if (this.generatedBy != null) {
            affectation.appendTaggable(this.generatedBy);
            affectation.appendString("affected");
            this.addressCreature(affectation, this.affected, false);
            affectation.appendString("!", null, null);
        } else {
            affectation.appendString("The affected one is");
            this.addressCreature(affectation, this.affected, false);
            affectation.appendString(".", null, null);
        }

        MultiRollResult damageResults = this.getDamages();
        if (damageResults != null && !damageResults.isEmpty()) {
            OutputBuilder damages = builder.produceSubBuilder("Damages");
            this.possesiveCreature(damages, this.affected);
            damages.appendString("health will change by");
            OutputBuilder healthDelta = damages.produceSubBuilder("Amount");
            healthDelta.appendTaggable(damageResults);
        }

        if (this.highlightedDelta == null) {
            if (this.isResultedInDeath()) {
                OutputBuilder deathNotice = builder.produceSubBuilder("Notice");
                deathNotice.appendString("As a result of these things");
                this.addressCreature(deathNotice, this.affected, false);
                deathNotice.appendString("has died.");
            }
            return;
        }

        if (this.highlightedDelta != null && this.highlightedDelta.getStatChanges().size() > 0) {
            OutputBuilder statChanges = builder.produceSubBuilder("Stats");
            for (Map.Entry<Stats, Integer> deltas : this.highlightedDelta.getStatChanges().entrySet()) {
                statChanges.appendString(deltas.getKey().toString(), "\r\n", " ").appendString("will change by")
                        .appendString(deltas.getValue().toString());
            }
        }

        if (this.isResultedInDeath()) {
            OutputBuilder deathNotice = builder.produceSubBuilder("Notice");
            deathNotice.appendString("As a result of these things");
            this.addressCreature(deathNotice, this.affected, false);
            deathNotice.appendString("has died.");
            return;
        }

        if (this.highlightedDelta.getAttributeScoreChanges().size() > 0) {
            OutputBuilder attributeScoreChanges = builder.produceSubBuilder("Scores");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeScoreChanges().entrySet()) {
                attributeScoreChanges.appendString(deltas.getKey().toString(), "\r\n", " ")
                        .appendString("score will change by").appendString(deltas.getValue().toString());
            }
        }

        if (this.highlightedDelta.getAttributeBonusChanges().size() > 0) {
            OutputBuilder attributeBonusChanges = builder.produceSubBuilder("Bonuses");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeBonusChanges().entrySet()) {
                attributeBonusChanges.appendString(deltas.getKey().toString(), "\r\n", " ")
                        .appendString("bonus will change by").appendString(deltas.getValue().toString());
            }
        }

        if (this.highlightedDelta.isRestoreFaction()) {
            this.possesiveCreature(builder, this.affected);
            builder.appendString("faction will be restored!");
        }
    }

}
