package com.lhf.game;

import java.util.Objects;

public class EntityEffector {
    public enum EffectPersistence {
        INSTANT, DURATION;
    }

    protected String generatedBy;
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

}
