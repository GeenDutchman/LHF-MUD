package com.lhf.game;

import java.util.Objects;

import com.lhf.TaggedExaminable;
import com.lhf.messages.events.SeeEvent;

public abstract class EntityEffectSource implements TaggedExaminable, Comparable<EntityEffectSource> {
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

    public abstract EntityEffectSource makeCopy();

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
    public SeeEvent produceMessage() {
        return SeeEvent.getBuilder().setExaminable(this).Build();
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
        return Objects.hash(className, name, persistence, resistance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EntityEffectSource))
            return false;
        EntityEffectSource other = (EntityEffectSource) obj;
        return Objects.equals(className, other.className) && Objects.equals(name, other.name)
                && Objects.equals(persistence, other.persistence) && Objects.equals(resistance, other.resistance);
    }

    @Override
    public final int compareTo(EntityEffectSource arg0) {
        if (this.equals(arg0)) {
            return 0;
        }
        int comparison = this.className.compareTo(arg0.className);
        if (comparison != 0) {
            return comparison;
        }
        comparison = this.name.compareTo(arg0.name);
        if (comparison != 0) {
            return comparison;
        }
        if (this.persistence == null && arg0.persistence != null) {
            return 1;
        } else if (this.persistence != null && arg0.persistence == null) {
            return -1;
        } else if (this.persistence != null && arg0.persistence != null) {
            comparison = this.persistence.compareTo(arg0.persistence);
            if (comparison != 0) {
                return comparison;
            }
        }

        return 0;
    }

    public abstract boolean isOffensive();

    public abstract int aiScore();

}