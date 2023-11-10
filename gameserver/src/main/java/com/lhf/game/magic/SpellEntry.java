package com.lhf.game.magic;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.ResourceCost;
import com.lhf.messages.out.CastingMessage;
import com.lhf.messages.out.SeeOutMessage;

public abstract class SpellEntry implements Taggable, Examinable, Comparable<SpellEntry> {
    private final String className;
    protected final ResourceCost level;
    protected final String name;
    protected final String invocation;
    protected String description;
    protected final Set<VocationName> allowedVocations;
    protected final Set<? extends EntityEffectSource> effectSources;

    public SpellEntry(ResourceCost level, String name, Set<? extends EntityEffectSource> effectSources,
            Set<VocationName> allowed,
            String description) {
        this.className = this.getClass().getName();
        this.level = level != null ? level : ResourceCost.NO_COST;
        this.name = name;
        this.description = description;
        this.invocation = name;
        this.allowedVocations = Set.copyOf(allowed);
        this.effectSources = Set.copyOf(effectSources);
    }

    public SpellEntry(ResourceCost level, String name, String invocation,
            Set<? extends EntityEffectSource> effectSources,
            Set<VocationName> allowed,
            String description) {
        this.className = this.getClass().getName();
        this.level = level != null ? level : ResourceCost.NO_COST;
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

    public int aiScore() {
        int score = 0;
        for (EntityEffectSource source : this.effectSources) {
            score += source.aiScore();
        }
        return score;
    }

    public String getClassName() {
        return this.className;
    }

    public ResourceCost getLevel() {
        return level;
    }

    public String getInvocation() {
        return invocation;
    }

    public Set<VocationName> getAllowedVocations() {
        return allowedVocations;
    }

    public Set<? extends EntityEffectSource> getEffectSources() {
        return this.effectSources;
    }

    abstract public CastingMessage Cast(Creature caster, ResourceCost castLevel, List<? extends Taggable> targets);

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

    protected String printEffectDescriptions() {
        StringBuilder sb = new StringBuilder();
        if (this.effectSources.size() > 0) {
            sb.append("\r\n");
            for (EntityEffectSource source : this.effectSources) {
                sb.append(source.printDescription()).append("\r\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String printDescription() {
        StringBuilder sb = new StringBuilder(this.description);
        sb.append(this.printEffectDescriptions());
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getColorTaggedName()).append("\r\n");
        sb.append("Level:").append(this.getLevel()).append("\r\n");
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
        int diff = this.getLevel().compareTo(other.getLevel());
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

    @Override
    public SeeOutMessage produceMessage() {
        return SeeOutMessage.getBuilder().setExaminable(this).Build();
    }
}
