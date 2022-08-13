package com.lhf.game.magic;

import java.util.Objects;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffector;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.Creature;
import com.lhf.messages.out.SeeOutMessage;

public abstract class ISpell implements EntityEffector, Taggable, Examinable {
    private final String className;
    protected final SpellEntry entry;
    protected transient Creature caster;
    protected Ticker timeLeft;

    public ISpell(SpellEntry entry) {
        this.className = this.getClass().getName();
        this.entry = entry;
        this.timeLeft = entry.getPersistence().getTicker();
    }

    public ISpell setCaster(Creature caster) {
        this.caster = caster;
        return this;
    }

    public String getClassName() {
        return this.className;
    }

    public Creature getCaster() {
        return this.caster;
    }

    public SpellEntry getEntry() {
        return entry;
    }

    public abstract boolean isOffensive();

    @Override
    public String getName() {
        return this.entry.getName();
    }

    public Integer getLevel() {
        return this.entry.getLevel();
    }

    public String getInvocation() {
        return this.entry.getInvocation();
    }

    public EffectPersistence getPersistence() {
        return this.entry.getPersistence();
    }

    @Override
    public Ticker getTicker() {
        return timeLeft;
    }

    @Override
    public String printDescription() {
        return this.entry.printDescription();
    }

    @Override
    public Creature creatureResponsible() {
        return this.caster;
    }

    @Override
    public Taggable getGeneratedBy() {
        return this.entry;
    }

    @Override
    public String getStartTag() {
        return this.entry.getStartTag();
    }

    @Override
    public String getEndTag() {
        return this.entry.getEndTag();
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public SeeOutMessage produceMessage() {
        SeeOutMessage seeOutMessage = new SeeOutMessage(this);
        return seeOutMessage;
    }

    @Override
    public String toString() {
        return this.entry.toString() + "Caster: " + this.caster.getColorTaggedName() + "\r\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(caster, className, entry);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ISpell)) {
            return false;
        }
        ISpell other = (ISpell) obj;
        return Objects.equals(caster, other.caster) && Objects.equals(className, other.className)
                && Objects.equals(entry, other.entry);
    }

    @Override
    public int compareTo(EntityEffector o) {
        return EntityEffector.super.compareTo(o);
    }

}
