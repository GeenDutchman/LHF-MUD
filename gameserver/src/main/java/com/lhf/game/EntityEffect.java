package com.lhf.game;

import java.util.Objects;

import com.lhf.Taggable;
import com.lhf.TaggedExaminable;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.server.interfaces.NotNull;

public abstract class EntityEffect implements TaggedExaminable, Comparable<EntityEffect> {

    protected final String className;
    protected final EntityEffectSource source;
    protected transient final ICreature creatureResponsible;
    protected final BasicTaggable generatedBy;
    protected final Ticker ticker;

    public EntityEffect(@NotNull EntityEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        this.className = this.getClass().getName();
        this.source = source;
        this.creatureResponsible = creatureResponsible;
        this.generatedBy = Taggable.basicTaggable(generatedBy);
        final EffectPersistence persistence = this.source.getPersistence();
        if (persistence != null) {
            this.ticker = persistence.getTicker();
        } else {
            this.ticker = null;
        }
    }

    public ICreature creatureResponsible() {
        return this.creatureResponsible;
    }

    public Taggable getGeneratedBy() {
        return this.generatedBy;
    }

    public boolean isOffensive() {
        return this.source.isOffensive();
    }

    public EffectPersistence getPersistence() {
        return this.source.getPersistence();
    }

    public EffectResistance getResistance() {
        return this.source.getResistance();
    }

    public boolean isReadyForRemoval() {
        return this.ticker != null ? this.ticker.getCountdown() == 0 : true;
    }

    public Ticker getTicker() {
        return this.ticker;
    }

    public boolean tick(GameEvent tickEvent) {
        if (tickEvent == null) {
            return false;
        }
        return this.ticker != null ? this.ticker.tick(tickEvent.getTickType()) : false;
    }

    @Override
    public final int compareTo(EntityEffect o) {
        if (this.equals(o)) {
            return 0;
        }
        return this.source.compareTo(o.source);
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public String printDescription() {
        return this.source.description + "\nIt will last "
                + (this.ticker == null ? this.getPersistence().toString() : this.ticker.toString());
    }

    @Override
    public SeeEvent produceMessage() {
        return this.source.produceMessage();
    }

    @Override
    public String getColorTaggedName() {
        return this.source.getColorTaggedName();
    }

    @Override
    public String getEndTag() {
        return this.source.getEndTag();
    }

    @Override
    public String getStartTag() {
        return this.source.getStartTag();
    }

    @Override
    public String toString() {
        return this.produceMessage().toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EntityEffect))
            return false;
        EntityEffect other = (EntityEffect) obj;
        return Objects.equals(source, other.source);
    }

}
