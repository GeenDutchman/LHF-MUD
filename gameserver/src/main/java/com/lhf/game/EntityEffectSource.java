package com.lhf.game;

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

}