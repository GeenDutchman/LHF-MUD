package com.lhf.game.magic;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffector.EffectPersistence;

public abstract class SpellEntry implements Taggable, Examinable {
    private final String className;
    protected final Integer level;
    protected final String name;
    protected final String invocation;
    protected final EffectPersistence persistence;
    protected String description;

    public SpellEntry(Integer level, String name, EffectPersistence persistence, String description) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.persistence = persistence;
        this.description = description;
        this.invocation = name;
    }

    public SpellEntry(Integer level, String name, String invocation, EffectPersistence persistence,
            String description) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.invocation = invocation;
        this.persistence = persistence;
        this.description = description;
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

    public EffectPersistence getPersistence() {
        return persistence;
    }

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
        sb.append(this.printDescription()).append("\r\n");
        return sb.toString();
    }
}
