package com.lhf.game;

import java.util.Objects;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.messages.out.SeeOutMessage;

public abstract class EntityEffectSource implements Taggable, Examinable {
    protected final String className;
    protected final String name;
    protected final EffectPersistence persistence;
    protected final EffectResistance resistance;
    protected String description;

    public EntityEffectSource(String name, EffectPersistence persistence, EffectResistance resistance,
            String description) {
        this.className = this.getClass().getName();
        this.name = name;
        this.persistence = persistence;
        this.resistance = resistance;
        this.description = description;
    }

    public EffectPersistence getPersistence() {
        return persistence;
    }

    public EffectResistance getResistance() {
        return resistance;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(this.description);
        if (this.persistence != null) {
            sb.append("\nIt will last ").append(this.persistence.toString());
        }
        if (this.resistance != null) {
            sb.append("\n").append(this.resistance.toString());
        }
        return sb.toString();
    }

    @Override
    public SeeOutMessage produceMessage() {
        return SeeOutMessage.getBuilder().setExaminable(this).Build();
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

    public abstract boolean isOffensive();

    public abstract int aiScore();

}