package com.lhf.game.magic;

import java.util.Objects;
import java.util.Set;

import com.lhf.Examinable;
import com.lhf.Taggable;
import com.lhf.game.EntityEffect;
import com.lhf.game.creature.Creature;
import com.lhf.game.enums.ResourceCost;
import com.lhf.messages.out.SeeOutMessage;

public abstract class ISpell<T extends EntityEffect>
        implements Comparable<ISpell<?>>, Iterable<T>, Taggable, Examinable {
    private final String className;
    protected final SpellEntry entry;
    protected transient Creature caster;

    public ISpell(SpellEntry entry, Creature caster) {
        this.className = this.getClass().getName();
        this.entry = entry;
        this.caster = caster;
    }

    public ISpell<T> setCaster(Creature caster) {
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

    public boolean isOffensive() {
        return this.entry.isOffensive();
    }

    @Override
    public String getName() {
        return this.entry.getName();
    }

    public ResourceCost getLevel() {
        return this.entry.getLevel();
    }

    public String getInvocation() {
        return this.entry.getInvocation();
    }

    public abstract Set<T> getEffects();

    @Override
    public String printDescription() {
        return this.entry.printDescription();
    }

    public Creature creatureResponsible() {
        return this.caster;
    }

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
        SeeOutMessage seeOutMessage = SeeOutMessage.getBuilder().setExaminable(this).Build();
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
        if (!(obj instanceof ISpell<?>)) {
            return false;
        }
        ISpell<?> other = (ISpell<?>) obj;
        return Objects.equals(caster, other.caster) && Objects.equals(className, other.className)
                && Objects.equals(entry, other.entry);
    }

    @Override
    public int compareTo(ISpell<?> o) {
        if (this.equals(o)) {
            return 0;
        }
        return this.entry.compareTo(o.getEntry());
    }

}
