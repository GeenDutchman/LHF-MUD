package com.lhf.game;

import java.util.Objects;

public class EntityEffector implements Comparable<EntityEffector> {
    public enum EffectPersistence {
        INSTANT, DURATION;
    }

    protected String generatedBy; // TODO: should this be taggable
    protected EffectPersistence persistence;

    public EntityEffector(String generatedBy, EffectPersistence persistence) {
        this.generatedBy = generatedBy;
        this.persistence = persistence;
    }

    public String getGeneratedBy() {
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
        int namecompare = this.generatedBy.compareTo(o.getGeneratedBy());
        if (namecompare != 0) {
            return namecompare;
        }
        return this.getPersistence().compareTo(o.getPersistence());
    }

}
