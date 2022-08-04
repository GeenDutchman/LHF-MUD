package com.lhf.game;

import java.util.Objects;

import com.lhf.Taggable;

public class EntityEffector implements Comparable<EntityEffector> {
    public enum EffectPersistence {
        INSTANT, DURATION;
    }

    protected Taggable generatedBy;
    protected EffectPersistence persistence;

    public EntityEffector(Taggable generatedBy, EffectPersistence persistence) {
        this.generatedBy = generatedBy;
        this.persistence = persistence;
    }

    public Taggable getGeneratedBy() {
        return generatedBy;
    }

    public EffectPersistence getPersistence() {
        return persistence;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityEffector [generatedBy=").append(generatedBy).append(", persistence=").append(persistence)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(generatedBy, persistence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityEffector)) {
            return false;
        }
        EntityEffector other = (EntityEffector) obj;
        return Objects.equals(generatedBy, other.generatedBy) && persistence == other.persistence;
    }

    @Override
    public int compareTo(EntityEffector o) {
        if (this.equals(o)) {
            return 0;
        }
        int namecompare = this.generatedBy.getColorTaggedName().compareTo(o.getGeneratedBy().getColorTaggedName());
        if (namecompare != 0) {
            return namecompare;
        }
        return this.getPersistence().compareTo(o.getPersistence());
    }

}
