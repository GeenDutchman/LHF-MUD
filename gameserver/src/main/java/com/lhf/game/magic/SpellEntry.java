package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffector.EffectPersistence;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.out.CastingMessage;

public abstract class SpellEntry implements Taggable, Examinable, Comparable<SpellEntry> {
    private final String className;
    protected final Integer level;
    protected final String name;
    protected final String invocation;
    protected final EffectPersistence persistence;
    protected String description;
    protected final List<VocationName> allowedVocations;

    public SpellEntry(Integer level, String name, EffectPersistence persistence, String description,
            VocationName... allowed) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.persistence = persistence;
        this.description = description;
        this.invocation = name;
        ArrayList<VocationName> vocations = new ArrayList<>();
        for (VocationName vocName : allowed) {
            vocations.add(vocName);
        }
        this.allowedVocations = Collections.unmodifiableList(vocations);
    }

    public SpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description, VocationName... allowed) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.invocation = invocation;
        this.persistence = persistence;
        this.description = description;
        ArrayList<VocationName> vocations = new ArrayList<>();
        for (VocationName vocName : allowed) {
            vocations.add(vocName);
        }
        this.allowedVocations = Collections.unmodifiableList(vocations);
    }

    public SpellEntry(SpellEntry other) {
        this.className = this.getClass().getName();
        this.level = other.level;
        this.name = other.name;
        this.invocation = other.invocation;
        this.persistence = other.persistence;
        this.description = new String(other.description);
        this.allowedVocations = Collections.unmodifiableList(other.allowedVocations);
    }

    public boolean Invoke(String invokeAttempt) {
        int invokeLen = this.getInvocation().length();
        if (invokeAttempt.length() < invokeLen) {
            return false;
        }
        String trimmedInvoke = invokeAttempt.substring(0, invokeLen);
        return this.getInvocation().equals(trimmedInvoke);
    }

    // public abstract ISpell create();

    public String getClassName() {
        return this.className;
    }

    public Integer getLevel() {
        return level;
    }

    public String getInvocation() {
        return invocation;
    }

    public EffectPersistence getPersistence() {
        return persistence;
    }

    public List<VocationName> getAllowedVocations() {
        return allowedVocations;
    }

    abstract public CastingMessage Cast(Creature caster, int castLevel, List<? extends Taggable> targets);

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String getEndTag() {
        return "</spell>";
    }

    @Override
    public String getStartTag() {
        return "<spell>";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String printDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getColorTaggedName()).append("\r\n");
        sb.append("Level:").append(this.getLevel()).append(" Persistence:").append(this.getPersistence())
                .append("\r\n");
        sb.append("Invocation:\"").append(this.getInvocation()).append("\"\r\n");
        sb.append("Can be cast by:");
        StringJoiner sj = new StringJoiner(", ").setEmptyValue("anyone with magical powers.");
        for (VocationName vocName : this.getAllowedVocations()) {
            sj.add(vocName.getColorTaggedName());
        }
        sb.append(sj.toString()).append("\r\n");
        sb.append(this.printDescription()).append("\r\n");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, description, invocation, level, name, persistence);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpellEntry)) {
            return false;
        }
        SpellEntry other = (SpellEntry) obj;
        return Objects.equals(className, other.className) && Objects.equals(description, other.description)
                && Objects.equals(invocation, other.invocation) && Objects.equals(level, other.level)
                && Objects.equals(name, other.name) && persistence == other.persistence;
    }

    @Override
    public int compareTo(SpellEntry other) {
        if (this.equals(other)) {
            return 0;
        }
        int diff = this.getLevel() - other.getLevel();
        if (diff != 0) {
            return diff;
        }
        diff = this.getName().compareTo(other.getName());
        if (diff != 0) {
            return diff;
        }
        diff = this.getInvocation().compareTo(other.getInvocation());
        if (diff != 0) {
            return diff;
        }
        diff = this.printDescription().compareTo(other.printDescription());
        if (diff != 0) {
            return diff;
        }
        diff = this.getClassName().compareTo(other.getClassName());
        if (diff != 0) {
            return diff;
        }
        return this.getPersistence().compareTo(other.getPersistence());
    }
}
