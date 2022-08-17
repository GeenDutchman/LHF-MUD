package com.lhf.game;

import java.util.Objects;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.messages.out.SeeOutMessage;

public abstract class EntityEffectSource implements Taggable, Examinable {
    protected final String name;
    protected final EffectPersistence persistence;
    protected String description;

    public EntityEffectSource(String name, EffectPersistence persistence, String description) {
        this.name = name;
        this.persistence = persistence;
        this.description = description;
    }

    public EffectPersistence getPersistence() {
        return persistence;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String printDescription() {
        return this.description + "\nIt will last " + this.persistence.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        return new SeeOutMessage(this);
    }

    @Override
    public String getStartTag() {
        return "<effect>";
    }

    @Override
    public String getEndTag() {
        return "</effect>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String toString() {
        return this.produceMessage().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, persistence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityEffectSource)) {
            return false;
        }
        EntityEffectSource other = (EntityEffectSource) obj;
        return Objects.equals(name, other.name)
                && Objects.equals(persistence, other.persistence);
    }

}