package com.lhf.messages.events;

import java.util.EnumSet;
import java.util.Map;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.Taggable;
import com.lhf.Taggable.BasicTaggable;
import com.lhf.game.creature.CreatureEffect;
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
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }
        Element affectation = nodeGenerator.createElement("Affectation");
        affectation.setAttribute("complex", "true");
        if (this.creatureResponsible != null) {
            affectation.appendChild(this.creatureResponsible.buildXMLElement(nodeGenerator));
            if (this.generatedBy != null) {
                affectation.appendChild(nodeGenerator.createTextNode(" used "));
                affectation.appendChild(this.generatedBy.buildXMLElement(nodeGenerator));
                affectation.appendChild(nodeGenerator.createTextNode(" on "));
            } else {
                affectation.appendChild(nodeGenerator.createTextNode(" affected "));
            }
            affectation.appendChild(this.addressCreatureXML(nodeGenerator, this.affected, false));
            affectation.appendChild(nodeGenerator.createTextNode("!"));
        } else if (this.generatedBy != null) {
            affectation.appendChild(this.generatedBy.buildXMLElement(nodeGenerator));
            affectation.appendChild(nodeGenerator.createTextNode(" affected "));
            affectation.appendChild(this.addressCreatureXML(nodeGenerator, this.affected, false));
            affectation.appendChild(nodeGenerator.createTextNode("!"));
        } else {
            affectation.appendChild(nodeGenerator.createTextNode("The affected one is "));
            affectation.appendChild(this.addressCreatureXML(nodeGenerator, this.affected, false));
        }
        myElement.appendChild(affectation);

        MultiRollResult damageResults = this.getDamages();
        if (damageResults != null && !damageResults.isEmpty()) {
            Element damages = nodeGenerator.createElement("Damages");
            damages.setAttribute("complex", "true");
            damages.appendChild(this.posessiveCreatureXML(nodeGenerator, this.affected, true));
            damages.appendChild(nodeGenerator.createTextNode(" health will change by "));
            Element healthDelta = nodeGenerator.createElement("HealthDelta");
            healthDelta.appendChild(damageResults.buildXMLElement(nodeGenerator));
            damages.appendChild(healthDelta);
            myElement.appendChild(damages);
        }
        if (this.highlightedDelta == null) {
            if (this.isResultedInDeath()) {
                Element deathNotice = nodeGenerator.createElement("DeathNotice");
                deathNotice.appendChild(nodeGenerator.createTextNode("And as a result of these things "));
                deathNotice.appendChild(this.addressCreatureXML(nodeGenerator, this.affected, false));
                deathNotice.appendChild(nodeGenerator.createTextNode(" has died."));
                myElement.appendChild(deathNotice);
            }
            return myElement;
        }
        if (this.highlightedDelta.getStatChanges().size() > 0) {
            Element statChanges = nodeGenerator.createElement("StatChanges");
            statChanges.setAttribute("complex", "true");
            for (Map.Entry<Stats, Integer> deltas : this.highlightedDelta.getStatChanges().entrySet()) {
                Element statChange = nodeGenerator.createElement("StatChange");
                Element stat = nodeGenerator.createElement(deltas.getKey().toString());
                stat.appendChild(nodeGenerator.createTextNode(" stat will change by "));
                Element value = nodeGenerator.createElement("StatChangeValue");
                value.setTextContent(Integer.toString(deltas.getValue()));
                stat.appendChild(value);
                statChange.appendChild(stat);
                statChanges.appendChild(statChange);
            }
            myElement.appendChild(statChanges);
        }
        if (this.isResultedInDeath()) {
            Element deathNotice = nodeGenerator.createElement("DeathNotice");
            deathNotice.appendChild(nodeGenerator.createTextNode("And as a result of these things "));
            deathNotice.appendChild(this.addressCreatureXML(nodeGenerator, this.affected, false));
            deathNotice.appendChild(nodeGenerator.createTextNode(" has died."));
            myElement.appendChild(deathNotice);
            return myElement;
        }
        if (this.highlightedDelta.getAttributeScoreChanges().size() > 0) {
            Element attributeScoreChanges = nodeGenerator.createElement("AttributeScoreChanges");
            attributeScoreChanges.setAttribute("complex", "true");
            attributeScoreChanges.appendChild(this.posessiveCreatureXML(nodeGenerator, this.affected, true));
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeScoreChanges().entrySet()) {
                Element attributeScoreChange = nodeGenerator.createElement("ScoreChange");
                Element attribute = nodeGenerator.createElement(deltas.getKey().toString());
                attribute.appendChild(nodeGenerator.createTextNode(" score will change by "));
                Element value = nodeGenerator.createElement("ScoreChangeValue");
                value.setTextContent(Integer.toString(deltas.getValue()));
                attribute.appendChild(value);
                attributeScoreChange.appendChild(attribute);
                attributeScoreChanges.appendChild(attributeScoreChange);
            }
            myElement.appendChild(attributeScoreChanges);
        }
        if (this.highlightedDelta.getAttributeBonusChanges().size() > 0) {
            Element attributeBonusChanges = nodeGenerator.createElement("AttributeBonusChanges");
            attributeBonusChanges.setAttribute("complex", "true");
            attributeBonusChanges.appendChild(this.posessiveCreatureXML(nodeGenerator, this.affected, true));
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeBonusChanges().entrySet()) {
                Element attributeBonusChange = nodeGenerator.createElement("BonusChange");
                Element attribute = nodeGenerator.createElement(deltas.getKey().toString());
                attribute.appendChild(nodeGenerator.createTextNode(" bonus will change by "));
                Element value = nodeGenerator.createElement("BonusChangeValue");
                value.setTextContent(Integer.toString(deltas.getValue()));
                attribute.appendChild(value);
                attributeBonusChange.appendChild(attribute);
                attributeBonusChanges.appendChild(attributeBonusChange);
            }
            myElement.appendChild(attributeBonusChanges);
        }
        if (this.highlightedDelta.isRestoreFaction()) {
            myElement.appendChild(this.posessiveCreatureXML(nodeGenerator, this.affected, true));
            myElement.appendChild(nodeGenerator.createTextNode(" faction will be restored!"));
        }
        return myElement;
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ");
        if (this.creatureResponsible != null) {
            sj.add(this.creatureResponsible.getName());
            if (this.generatedBy != null) {
                sj.add("used").add(this.generatedBy.getSimpleContent()).add("on");
            } else {
                sj.add("affected");
            }
            sj.add(this.addressCreature(this.affected, false) + "!");
        } else if (this.generatedBy != null) {
            sj.add(this.generatedBy.getSimpleContent()).add("affected")
                    .add(this.addressCreature(this.affected, false) + "!");
        } else {
            sj.add(this.addressCreature(creatureResponsible, false)).add("is affected!");
        }
        sj.add("\r\n");
        MultiRollResult damageResults = this.getDamages();
        if (damageResults != null && !damageResults.isEmpty()) {
            if (!this.isBroadcast()) {
                sj.add("Your");
            } else if (this.affected != null) {
                sj.add(this.affected.getSimpleContent() + "'s");
            } else {
                sj.add("Their");
            }
            sj.add("health will change by");
            sj.add(damageResults.toString()); // already reversed, if applicable
            sj.add("\r\n");
        }
        if (this.highlightedDelta == null) {
            return sj.toString();
        }
        if (this.highlightedDelta.getStatChanges().size() > 0) {
            sj.add(this.affected.getName() + "'s");
            for (Map.Entry<Stats, Integer> deltas : this.highlightedDelta.getStatChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("stat will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.isResultedInDeath()) {
            sj.add("And as a result of these things,").add(this.affected.getName()).add("has died.");
            return sj.toString();
        }
        if (this.highlightedDelta.getAttributeScoreChanges().size() > 0) {
            sj.add(this.affected.getName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeScoreChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("score will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.highlightedDelta.getAttributeBonusChanges().size() > 0) {
            sj.add(this.affected.getName() + "'s");
            for (Map.Entry<Attributes, Integer> deltas : this.highlightedDelta.getAttributeBonusChanges().entrySet()) {
                int amount = deltas.getValue();
                sj.add(deltas.getKey().toString()).add("bonus will change by").add(String.valueOf(amount));
            }
            sj.add("\r\n");
        }
        if (this.highlightedDelta.isRestoreFaction()) {
            sj.add(this.affected.getName() + "'s").add("faction will be restored!");
        }
        return sj.toString();
    }

}
