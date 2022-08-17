package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.out.CastingMessage;

public abstract class SpellEntry implements Taggable, Examinable, Comparable<SpellEntry> {
    private final String className;
    protected final Integer level;
    protected final String name;
    protected final String invocation;
    protected String description;
    protected final Set<VocationName> allowedVocations;
    protected final Set<EntityEffectSource> effectSources;

    public SpellEntry(Integer level, String name, Set<EntityEffectSource> effectSources, Set<VocationName> allowed,
            String description) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.description = description;
        this.invocation = name;
        this.allowedVocations = Set.copyOf(allowed);
        this.effectSources = Set.copyOf(effectSources);
    }

    public SpellEntry(Integer level, String name, String invocation, Set<EntityEffectSource> effectSources,
            Set<VocationName> allowed,
            String description) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.invocation = invocation;
        this.description = description;
        this.allowedVocations = Set.copyOf(allowed);
        this.effectSources = Set.copyOf(effectSources);
    }

    public SpellEntry(SpellEntry other) {
        this.className = this.getClass().getName();
        this.level = other.level;
        this.name = other.name;
        this.invocation = other.invocation;
        this.description = new String(other.description);
        this.allowedVocations = other.allowedVocations;
        this.effectSources = other.effectSources;
    }

    public boolean Invoke(String invokeAttempt) {
        int invokeLen = this.getInvocation().length();
        if (invokeAttempt.length() < invokeLen) {
            return false;
        }
        String trimmedInvoke = invokeAttempt.substring(0, invokeLen);
        return this.getInvocation().equals(trimmedInvoke);
    }

    public boolean isOffensive() {
        for (EntityEffectSource source : this.effectSources) {
            if (source.isOffensive()) {
                return true;
            }
        }
        return false;
    }

    public String getClassName() {
        return this.className;
    }

    public Integer getLevel() {
        return level;
    }

    public String getInvocation() {
        return invocation;
    }

    public Set<VocationName> getAllowedVocations() {
        return allowedVocations;
    }

    public Set<EntityEffectSource> getEffectSources() {
        return this.effectSources;
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
        StringBuilder sb = new StringBuilder(this.description);
        if (this.effectSources.size() > 0) {
            sb.append("\r\n");
            for (EntityEffectSource source : this.effectSources) {
                sb.append(source.printDescription()).append("\r\n");
            }
        }
        return sb.toString();
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
        return Objects.hash(allowedVocations, className, effectSources, invocation, level, name);
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
        return Objects.equals(allowedVocations, other.allowedVocations) && Objects.equals(className, other.className)
                && Objects.equals(effectSources, other.effectSources) && Objects.equals(invocation, other.invocation)
                && Objects.equals(level, other.level) && Objects.equals(name, other.name);
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
        return this.getEffectSources().size() - other.getEffectSources().size();
    }
}
