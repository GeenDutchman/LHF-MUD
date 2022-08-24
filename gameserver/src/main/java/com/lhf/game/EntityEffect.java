package com.lhf.game;

import java.util.Objects;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.Creature;
import com.lhf.messages.out.SeeOutMessage;

public abstract class EntityEffect implements Examinable, Taggable, Comparable<EntityEffect> {

    protected final EntityEffectSource source;
    protected Creature creatureResponsible;
    protected Taggable generatedBy;
    protected Ticker ticker;

    public EntityEffect(EntityEffectSource source, Creature creatureResponsible, Taggable generatedBy) {
        this.source = source;
        this.creatureResponsible = creatureResponsible;
        this.generatedBy = generatedBy;
        this.ticker = null;
    }

    public Creature creatureResponsible() {
        return this.creatureResponsible;
    }

    public Taggable getGeneratedBy() {
        return this.generatedBy;
    }

    public boolean isOffensive() {
        return this.source.isOffensive();
    }

    public EffectPersistence getPersistence() {
        return this.source.getPersistence();
    }

    public Ticker getTicker() {
        if (this.ticker == null) {
            this.ticker = this.source.getPersistence().getTicker();
        }
        return this.ticker;
    }

    public int tick(TickType type) {
        return this.getTicker().tick(type);
    }

    @Override
    public int compareTo(EntityEffect o) {
        if (this.equals(o)) {
            return 0;
        }
        int namecompare = this.getGeneratedBy().getColorTaggedName().compareTo(o.getGeneratedBy().getColorTaggedName());
        if (namecompare != 0) {
            return namecompare;
        }
        return this.getPersistence().compareTo(o.getPersistence());
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public String printDescription() {
        return this.source.description + "\nIt will last "
                + (this.ticker == null ? this.getPersistence().toString() : this.ticker.toString());
    }

    @Override
    public SeeOutMessage produceMessage() {
        return this.source.produceMessage();
    }

    @Override
    public String getColorTaggedName() {
        return this.source.getColorTaggedName();
    }

    @Override
    public String getEndTag() {
        return this.source.getEndTag();
    }

    @Override
    public String getStartTag() {
        return this.source.getStartTag();
    }

    @Override
    public String toString() {
        return this.produceMessage().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityEffect)) {
            return false;
        }
        EntityEffect other = (EntityEffect) obj;
        return Objects.equals(source, other.source);
    }

}